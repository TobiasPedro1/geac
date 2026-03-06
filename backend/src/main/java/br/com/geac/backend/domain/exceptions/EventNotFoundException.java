package br.com.geac.backend.domain.exceptions;

public class EventNotFoundException extends BadRequestException {
    public EventNotFoundException(String message) {
        super(message);
    }
}
