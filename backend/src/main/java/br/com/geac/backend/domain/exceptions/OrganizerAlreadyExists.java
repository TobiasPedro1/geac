package br.com.geac.backend.domain.exceptions;

public class OrganizerAlreadyExists extends ConflictException {
    public OrganizerAlreadyExists(String message) {
        super(message);
    }
}
