package br.com.geac.backend.domain.exceptions;

public class RequestAlreadyExists extends ConflictException {
    public RequestAlreadyExists(String message) {
        super(message);
    }
}
