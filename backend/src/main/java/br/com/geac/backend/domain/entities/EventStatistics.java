package br.com.geac.backend.domain.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

import java.util.UUID;

@Entity
@Table(name = "vw_eventos_estatisticas")
@Immutable
@Getter

public class EventStatistics {

    @Getter
    @Id
    private UUID eventId;

    private String eventTitle;
    private String eventStatus;
    private Integer totalInscritos;
    private Integer totalPresentes;
    private Double mediaAvaliacao;

}