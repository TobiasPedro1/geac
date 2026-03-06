package br.com.geac.backend.infrastucture.repositories;

import br.com.geac.backend.domain.entities.EventRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRequirementRepository extends JpaRepository<EventRequirement, Integer> {
}