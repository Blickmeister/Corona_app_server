package uhk.fim.smap.corona_app_server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sun.istack.NotNull;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "corona_information")
public class CoronaInformation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "corona_information_id")
    private Long id;

    @NotNull
    @Column(name = "corona_information_region_code",  unique = true)
    private String regionCode;

    @NotNull
    @Column(name = "corona_information_region_name",  unique = true)
    private String regionName;

    @NotNull
    @Column(name = "corona_information_last_date")
    private String lastDate;

    @Column(name = "corona_information_actual_cases")
    @ElementCollection
    private List<Integer> actualNumberOfCases;

    @Column(name = "corona_information_future_cases")
    @ElementCollection(fetch = FetchType.EAGER)
    private List<Double> futureNumberOfCases;

    @Column(name="corona_information_cured_number")
    private int numberOfCured;

    @Column(name="corona_information_death_number")
    private int numberOfDeath;

    /*@OneToMany(mappedBy = "coronaInformation")
    private List<ActualNumberOfCases> actualNumberOfCases;

    @OneToMany(mappedBy = "coronaInformation")
    private List<PredictedNumberOfCases> predictedNumberOfCases;*/

    public CoronaInformation() {
    }

   /* public CoronaInformation(Long id, String regionCode, String regionName, String lastDate, List<ActualNumberOfCases> actualNumberOfCases, List<PredictedNumberOfCases> predictedNumberOfCases) {
        this.id = id;
        this.regionCode = regionCode;
        this.regionName = regionName;
        this.lastDate = lastDate;
        this.actualNumberOfCases = actualNumberOfCases;
        this.predictedNumberOfCases = predictedNumberOfCases;
    }*/

    public CoronaInformation(Long id, String regionCode, String regionName, String lastDate, List<Integer> actualNumberOfCases, List<Double> futureNumberOfCases, int numberOfDeath, int numberOfCured) {
        this.id = id;
        this.regionCode = regionCode;
        this.regionName = regionName;
        this.lastDate = lastDate;
        this.actualNumberOfCases = actualNumberOfCases;
        this.futureNumberOfCases = futureNumberOfCases;
        this.numberOfDeath = 0;
        this.numberOfCured = 0;
    }

    public CoronaInformation(String regionCode) {
        this.regionCode = regionCode;
        this.regionName = getRegionNameByCode(regionCode);
        this.numberOfDeath = 0;
        this.numberOfCured = 0;
    }

    public String getRegionNameByCode(String regionCode) {
        String regionName = "";
        switch (regionCode) {
            case "CZ010":
                regionName = "Hlavní město Praha";
                break;
            case "CZ031":
                regionName = "Jihočeský kraj";
                break;
            case "CZ064":
                regionName = "Jihomoravský kraj";
                break;
            case "CZ041":
                regionName = "Karlovarský kraj";
                break;
            case "CZ052":
                regionName = "Královéhradecký kraj";
                break;
            case "CZ051":
                regionName = "Liberecký kraj";
                break;
            case "CZ080":
                regionName = "Moravskoslezský kraj";
                break;
            case "CZ071":
                regionName = "Olomoucký kraj";
                break;
            case "CZ053":
                regionName = "Pardubický kraj";
                break;
            case "CZ032":
                regionName = "Plzeňský kraj";
                break;
            case "CZ020":
                regionName = "Středočeský kraj";
                break;
            case "CZ042":
                regionName = "Ústecký kraj";
                break;
            case "CZ063":
                regionName = "Vysočina";
                break;
            case "CZ072":
                regionName = "Zlínský kraj";
                break;
        }
        return regionName;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public void setNumberOfCured(int numberOfCured) {
        this.numberOfCured = numberOfCured;
    }

    public void setNumberOfDeath(int numberOfDeath) {
        this.numberOfDeath = numberOfDeath;
    }

    /*public void setActualNumberOfCases(List<ActualNumberOfCases> actualNumberOfCases) {
        this.actualNumberOfCases = actualNumberOfCases;
    }*/

    /*public void setPredictedNumberOfCases(List<PredictedNumberOfCases> predictedNumberOfCases) {
        this.predictedNumberOfCases = predictedNumberOfCases;
    }*/

    public void setActualNumberOfCases(List<Integer> actualNumberOfCases) {
        this.actualNumberOfCases = actualNumberOfCases;
    }

    public void setFutureNumberOfCases(List<Double> futureNumberOfCases) {
        this.futureNumberOfCases = futureNumberOfCases;
    }

    public void setLastDate(String lastDate) {
        this.lastDate = lastDate;
    }

    public Long getId() {
        return id;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public String getRegionName() {
        return regionName;
    }

    /*public List<ActualNumberOfCases> getActualNumberOfCases() {
        return actualNumberOfCases;
    }

    public List<PredictedNumberOfCases> getPredictedNumberOfCases() {
        return predictedNumberOfCases;
    }*/

    public String getLastDate() {
        return lastDate;
    }

    public List<Integer> getActualNumberOfCases() {
        return actualNumberOfCases;
    }

    public List<Double> getFutureNumberOfCases() {
        return futureNumberOfCases;
    }

    public int getNumberOfCured() {
        return numberOfCured;
    }

    public int getNumberOfDeath() {
        return numberOfDeath;
    }
}
