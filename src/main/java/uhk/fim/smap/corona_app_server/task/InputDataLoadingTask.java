package uhk.fim.smap.corona_app_server.task;

import org.apache.commons.lang3.ArrayUtils;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler;
import org.nd4j.linalg.factory.Nd4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import uhk.fim.smap.corona_app_server.model.CoronaInformation;
import uhk.fim.smap.corona_app_server.repository.CoronaInformationRepository;
import uhk.fim.smap.corona_app_server.service.CustomWebClientService;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class InputDataLoadingTask {

    private static final int DISTRICT_NUMBER = 77; // počet okresů
    private static final String[] REGION_CODES = {"CZ010", "CZ031", "CZ064", "CZ041", "CZ052", "CZ051", "CZ080",
            "CZ071", "CZ053", "CZ032", "CZ020", "CZ042", "CZ063", "CZ072"};
    private CoronaInformationRepository coronaInformationRepository;

    @Autowired
    public InputDataLoadingTask(CoronaInformationRepository coronaInformationRepository) {
        this.coronaInformationRepository = coronaInformationRepository;
    }

    // každý den v 1:20 a 1:40 se stáhnou aktuální data z MZCR
    //@Scheduled(cron = "0 20,40 1 * * *")
    @Scheduled(fixedDelay = 400000) // pro testování
    public void downloadData() throws IOException {
        // stáhnutí
        CustomWebClientService customWebClientService = new CustomWebClientService(WebClient.builder());
        DataBuffer dataBuffer = customWebClientService.getData();

        // zpracování získaných dat
        BufferedReader csvReader = new BufferedReader(new InputStreamReader(dataBuffer.asInputStream()));
        String csvRow; // 1 řádek
        List<String[]> data = new ArrayList<>(); // všechny řádky
        while ((csvRow = csvReader.readLine()) != null) {
            String[] rowData = csvRow.split(",");
            data.add(rowData);
        }
        csvReader.close();
        DataBufferUtils.release(dataBuffer);

        Collections.reverse(data); // seřazeno od nejnovějších hodnot

        NNPredictionTask.loadModel(); // načtení modelu není-li již načten

        // pro každý kraj
        for (int k = 0; k < REGION_CODES.length; k++) {
            // získání objektu kraje
            CoronaInformation information = getCoronaInformationForRegions(REGION_CODES[k]);
            List<Integer> actualNumberOfCases = information.getActualNumberOfCases();
            List<Double> predictionsInRegion = information.getFutureNumberOfCases();
            int numberOfCuredInRegion = 0;
            int numberOfDeathInRegion = 0;
            // získání počtu vyléčených a mrtvých pouze v posledním dni
            for(int l = 1; l <= DISTRICT_NUMBER+1; l++) {
                String[] actualRow = data.get(l);
                if (actualRow[1].equals(REGION_CODES[k])) {
                    numberOfCuredInRegion += Integer.parseInt(actualRow[4]);
                    numberOfDeathInRegion += Integer.parseInt(actualRow[5]);
                }
            }
            // uložení informací o aktuálním počtu mrtvých a vyléčených a aktuální datum dat
            information.setNumberOfCured(numberOfCuredInRegion);
            information.setNumberOfDeath(numberOfDeathInRegion);
            String[] lastRow = data.get(0);
            information.setLastDate(lastRow[0]);
            // získání počtu nakažených v kraji dle datumu a kódu kraje
            for (int i = 1; i <= (DISTRICT_NUMBER+1) * 30; i += DISTRICT_NUMBER+1) { // pro 30 dní
                int totalNumberPerDayInRegion = 0;
                for (int j = i; j <= i + DISTRICT_NUMBER; j++) { // součet v jednom dni
                    String[] actualRow = data.get(j);
                    if (actualRow[1].equals(REGION_CODES[k])) {
                        totalNumberPerDayInRegion += Integer.parseInt(actualRow[3]);
                    }
                }
                // přidání do listu počtu nakažených v příslušném kraji
                //System.out.println(totalNumberPerDayInRegion);
                actualNumberOfCases.add(totalNumberPerDayInRegion);
            }
            // incializace vstupních testovacích dat do NN pro predikci
            double[] inputData = new double[14];
            double[] outputData = new double[1];
            outputData[0] = actualNumberOfCases.get(0);
            int indexInActualNumberOfCases = 1;
            int firstTimestep = actualNumberOfCases.get(1);
            inputData[0] = firstTimestep;
            int indexInPredictionsInRegion = 0;
            // predikování 30 budoucích hodnot
            for(int i = 0; i < 30; i++) {
                // definice vstupních dat
                if(predictionsInRegion.isEmpty()) { // nemáme-li ještě žádnou predikci
                    indexInActualNumberOfCases++;
                    // získání 14ti předešlých hodnot pro poslední aktuální hodnotu
                    for (int j = 1; j <= 13; j++) {
                        inputData[j] = actualNumberOfCases.get(++indexInActualNumberOfCases);
                    }
                } else { // máme-li již nějakou hodnotu predikce
                    // převod na list - jednodušší manipulace
                    Double[] tmpArray = ArrayUtils.toObject(inputData);
                    List<Double> inputDataList = Arrays.asList(tmpArray);
                    List<Double> inputDataListTmp = new ArrayList<>(inputDataList); // aby šlo modifikovat
                    // přidání prvku na začátek
                    if (predictionsInRegion.size() == 1) { // je-li pouze jediná hodnota predikce
                        // prvním prvkem vstupních dat je poslední aktuální hodnota
                        inputDataListTmp.add(0, new Double(actualNumberOfCases.get(0)));
                    } else {
                        // jinak prvním prvkem vstupních dat je předposlední hodnota predikce
                        inputDataListTmp.add(0, predictionsInRegion.get(predictionsInRegion.size() - 2));
                    }
                    inputDataListTmp.remove(inputDataListTmp.size()-1); // odebrání posledního

                    // převod zpátky na pole pro přístup k NN
                    inputData = inputDataListTmp.stream().mapToDouble(d -> d).toArray();
                    // aktuální hodnotou je vždy poslední hodnota predikce
                    outputData[0] = predictionsInRegion.get(predictionsInRegion.size() - 1);
                }

                // příprava dat pro škálování
                INDArray inputDataForScaling = Nd4j.create(inputData);
                INDArray outputDataForScaling = Nd4j.create(outputData);
                INDArray inputDataToNNFinal = Nd4j.expandDims(inputDataForScaling, 1);
                INDArray outputDataToNNFinal = Nd4j.expandDims(outputDataForScaling, 1);
                // škálování na rozsah 0.01 - 0.99
                NormalizerMinMaxScaler scaler = new NormalizerMinMaxScaler(0.01, 0.99);
                DataSet dataSet = new DataSet(inputDataToNNFinal, outputDataToNNFinal);
                scaler.fit(dataSet);
                scaler.transform(dataSet);
                // úprava pro vstup do NN
                INDArray inputDataScaled = Nd4j.squeeze(dataSet.getFeatures(), 1);
                INDArray inputDataToNNScaled = Nd4j.expandDims(inputDataScaled, 0);

                // získání predikce z modelu
                double prediction = NNPredictionTask.getPrediction(inputDataToNNScaled);
                // škálování na původní rozsah
                double[] predictionArray = {prediction};
                INDArray predictionArrayToScale = Nd4j.create(predictionArray);
                scaler.revertFeatures(predictionArrayToScale);
                // uložení predikované hodnoty
                predictionsInRegion.add(predictionArrayToScale.getDouble(0));
            }
            // uložení predikce k příslušnému kraji
            information.setFutureNumberOfCases(predictionsInRegion);
            coronaInformationRepository.save(information); // uložení nové informace v kraji do DB
        }
        System.out.println("DATA ZÍSKÁNA A ZPRACOVÁNA");
    }



    private CoronaInformation getCoronaInformationForRegions(String regionCode) {
        CoronaInformation information = coronaInformationRepository.findByRegionCode(regionCode);
        if (information == null) {
            CoronaInformation newInformation = new CoronaInformation(regionCode);
            newInformation.setActualNumberOfCases(new ArrayList<>());
            newInformation.setFutureNumberOfCases(new ArrayList<>());
            return newInformation;
        } else {
            information.setActualNumberOfCases(new ArrayList<>());
            information.setFutureNumberOfCases(new ArrayList<>());
            return information;
        }
    }
}
