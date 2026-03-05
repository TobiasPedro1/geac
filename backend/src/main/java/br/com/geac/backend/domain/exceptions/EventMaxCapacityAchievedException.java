package br.com.geac.backend.domain.exceptions;

public class EventMaxCapacityAchievedException extends BadRequestException {
    public EventMaxCapacityAchievedException(String message) {
        super(message);
    }
}
