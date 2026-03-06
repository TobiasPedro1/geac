package br.com.geac.backend.domain.exceptions;

public class SpeakerNotFoundException extends BadRequestException {
    public SpeakerNotFoundException(String message) {
        super(message);
    }
}
