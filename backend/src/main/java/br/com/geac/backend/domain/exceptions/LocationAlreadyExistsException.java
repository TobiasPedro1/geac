package br.com.geac.backend.domain.exceptions;

public class LocationAlreadyExistsException extends ConflictException {
    public LocationAlreadyExistsException(String message) {
        super(message);
    }
}
