package br.com.geac.backend.infrastucture.repositories;

import br.com.geac.backend.domain.entities.OrganizationEngagement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrganizationEngagementRepository extends JpaRepository<OrganizationEngagement, UUID> {
}
