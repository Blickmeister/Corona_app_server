package uhk.fim.smap.corona_app_server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uhk.fim.smap.corona_app_server.model.CoronaInformation;
import uhk.fim.smap.corona_app_server.repository.CoronaInformationRepository;

@RequestMapping("/corona/info")
@RestController
public class CoronaInformationController {

    private CoronaInformationRepository coronaInformationRepository;

    @Autowired
    public CoronaInformationController(CoronaInformationRepository coronaInformationRepository) {
        this.coronaInformationRepository = coronaInformationRepository;
    }

    @RequestMapping(value = "/region/{code}", method = RequestMethod.GET)
    public CoronaInformation getByCode(@PathVariable(value = "code") String regionCode) {
        return coronaInformationRepository.findByRegionCode(regionCode);
    }
}
