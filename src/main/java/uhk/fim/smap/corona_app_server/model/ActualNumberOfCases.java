package uhk.fim.smap.corona_app_server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sun.istack.NotNull;

import javax.persistence.*;

@Entity
@Table(name = "actual_cases_number")
public class ActualNumberOfCases {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "actual_cases_number_id")
    private Long id;

    @NotNull
    @Column(name = "actual_cases_number_value")
    private double value;

    @JsonIgnore
    @ManyToOne
    private CoronaInformation coronaInformation;

    public ActualNumberOfCases() {
    }

    public ActualNumberOfCases(Long id, double value, CoronaInformation coronaInformation) {
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
