package br.com.geac.backend.domain.exceptions;

public class SpeakerAlreadyExistsException extends ConflictException {
    public SpeakerAlreadyExistsException(String message) {
        super(message);
    }
}
