package br.com.geac.backend.domain.exceptions;

public class OrganizerNotFoundExceptio extends BadRequestException {
    public OrganizerNotFoundExceptio(String message) {
        super(message);
    }
}
