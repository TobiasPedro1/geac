package br.com.geac.backend.infrastucture.repositories;

import br.com.geac.backend.domain.entities.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationRepository extends JpaRepository<Location, Integer> {


    boolean existsByZipCodeAndNumberAndNameAndIdNot(String zipCode, String number, String name, Integer id);

    boolean existsByZipCodeAndNumberAndName(String zipCode, String number, String name);
}