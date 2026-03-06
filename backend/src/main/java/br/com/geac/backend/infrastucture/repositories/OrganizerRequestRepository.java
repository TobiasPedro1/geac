package br.com.geac.backend.infrastucture.repositories;

import br.com.geac.backend.domain.entities.OrganizerRequest;
import br.com.geac.backend.domain.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrganizerRequestRepository extends JpaRepository<OrganizerRequest, Integer> {
    List<OrganizerRequest> findAllByStatus(RequestStatus status);

    boolean existsByUserIdAndOrganizerIdAndStatus(UUID userId, UUID organizerId, RequestStatus status);
}