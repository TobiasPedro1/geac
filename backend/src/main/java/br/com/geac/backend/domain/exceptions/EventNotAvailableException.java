package br.com.geac.backend.domain.exceptions;

public class EventNotAvailableException extends BadRequestException {
    public EventNotAvailableException(String message) {
        super(message);
    }
}
