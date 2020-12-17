package uhk.fim.smap.corona_app_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uhk.fim.smap.corona_app_server.model.CoronaInformation;

@Repository
public interface CoronaInformationRepository extends JpaRepository<CoronaInformation, Long> {
    CoronaInformation findByRegionCode(String code);


}
