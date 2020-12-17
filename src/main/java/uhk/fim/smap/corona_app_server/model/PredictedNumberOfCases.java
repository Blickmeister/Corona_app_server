package uhk.fim.smap.corona_app_server.model;

import com.sun.istack.NotNull;

import javax.persistence.*;

@Entity
@Table(name = "predicted_cases_number")
public class PredictedNumberOfCases {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "predicted_cases_number_id")
    private Long id;

    @NotNull
    @Column(name = "predicted_cases_number_value")
    private double value;

    @ManyToOne
    private CoronaInformation coronaInformation;

    public PredictedNumberOfCases() {
    }

    public PredictedNumberOfCases(Long id, double value, CoronaInformation coronaInformation) {
        this.id = id;
        this.value = value;
        this.coronaInformation = coronaInformation;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void setCoronaInformation(CoronaInformation coronaInformation) {
        this.coronaInformation = coronaInformation;
    }

    public Long getId() {
        return id;
    }

    public double getValue() {
        return value;
    }

    public CoronaInformation getCoronaInformation() {
        return coronaInformation;
    }
}
