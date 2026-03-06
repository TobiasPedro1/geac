package br.com.geac.backend.domain.exceptions;

public class EventNotFinishedException extends BadRequestException {
    public EventNotFinishedException(String message) {
        super(message);
    }
}
