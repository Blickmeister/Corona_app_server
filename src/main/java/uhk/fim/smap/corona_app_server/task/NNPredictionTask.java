package uhk.fim.smap.corona_app_server.task;

import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.deeplearning4j.nn.modelimport.keras.exceptions.InvalidKerasConfigurationException;
import org.deeplearning4j.nn.modelimport.keras.exceptions.UnsupportedKerasConfigurationException;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.common.io.ClassPathResource;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class NNPredictionTask {

    private static MultiLayerNetwork model;

    public static void loadModel() {
        if(model == null) {
            try {
                // načtení modelu
                String gamesMlp = new ClassPathResource("/FNN_model.h5").getFile().getPath();
                model = KerasModelImport.importKerasSequentialModelAndWeights(gamesMlp);
            } catch (IOException | UnsupportedKerasConfigurationException | InvalidKerasConfigurationException e) {
                e.printStackTrace();
            }
        }
    }

    public NNPredictionTask() {
    }

    public static double getPrediction(INDArray features) {
        return model.output(features).getDouble(0);
    }
}
