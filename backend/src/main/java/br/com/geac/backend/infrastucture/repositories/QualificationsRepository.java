package br.com.geac.backend.infrastucture.repositories;

import br.com.geac.backend.domain.entities.Qualification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QualificationsRepository extends JpaRepository<Qualification, Integer> {
}
