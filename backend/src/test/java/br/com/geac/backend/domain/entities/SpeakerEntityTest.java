package br.com.geac.backend.domain.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SpeakerEntityTest {

    @Test
    @DisplayName("setQualifications deve tratar conjuntos nulos e nao nulos")
    void setQualifications_Coverage() {
        Speaker speaker = new Speaker();
        Qualification q = new Qualification();
        q.setTitleName("MSc");
        q.setInstitution("UFAPE");

        speaker.setQualifications(Set.of(q));
        assertThat(speaker.getQualifications()).hasSize(1);
        assertThat(q.getSpeaker()).isEqualTo(speaker);

        speaker.setQualifications(null);
        assertThat(speaker.getQualifications()).isEmpty();
    }
}


