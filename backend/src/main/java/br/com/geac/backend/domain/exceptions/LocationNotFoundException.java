package br.com.geac.backend.domain.exceptions;

public class LocationNotFoundException extends BadRequestException {
    public LocationNotFoundException(String message) {
        super(message);
    }
}
