package br.com.geac.backend.domain.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class EventStatusConverter implements AttributeConverter<EventStatus, String> {

    @Override
    public String convertToDatabaseColumn(EventStatus status) {
        if (status == null) {
            return null;
        }
        return status.name();
    }

    @Override
    public EventStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        // Converte o valor do banco para maiúsculo antes de mapear
        try {
            return EventStatus.valueOf(dbData.toUpperCase());
        } catch (IllegalArgumentException _) {
            return null;
        }
    }
}