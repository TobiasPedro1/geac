package br.com.geac.backend.domain.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConvertersTest {

    @Test
    @DisplayName("CampusConverter deve tratar valores nulos, validos e invalidos")
    void campusConverter_Coverage() {
        CampusConverter converter = new CampusConverter();

        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToDatabaseColumn(Campus.CAMPUS_RECIFE_CENTRAL))
                .isEqualTo(Campus.CAMPUS_RECIFE_CENTRAL.getDescricao());

        assertThat(converter.convertToEntityAttribute(null)).isNull();
        assertThat(converter.convertToEntityAttribute(Campus.CAMPUS_SURUBIM_SUL.getDescricao()))
                .isEqualTo(Campus.CAMPUS_SURUBIM_SUL);
        assertThatThrownBy(() -> converter.convertToEntityAttribute("unknown campus"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("EventStatusConverter deve tratar valores nulos, validos e invalidos")
    void eventStatusConverter_Coverage() {
        EventStatusConverter converter = new EventStatusConverter();

        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToDatabaseColumn(EventStatus.ACTIVE)).isEqualTo("ACTIVE");

        assertThat(converter.convertToEntityAttribute(null)).isNull();
        assertThat(converter.convertToEntityAttribute("completed")).isEqualTo(EventStatus.COMPLETED);
        assertThat(converter.convertToEntityAttribute("invalid-status")).isNull();
    }
}


