package br.com.geac.backend.infrastucture.repositories;

import br.com.geac.backend.domain.entities.StudentExtracurricularHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StudentExtracurricularHoursRepository extends JpaRepository<StudentExtracurricularHours, UUID> {
    // O JpaRepository já provê o findById (que usará o studentId)
}