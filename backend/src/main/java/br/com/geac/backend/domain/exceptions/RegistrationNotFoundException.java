package br.com.geac.backend.domain.exceptions;

public class RegistrationNotFoundException extends BadRequestException {
    public RegistrationNotFoundException(String message) {
        super(message);
    }
}
