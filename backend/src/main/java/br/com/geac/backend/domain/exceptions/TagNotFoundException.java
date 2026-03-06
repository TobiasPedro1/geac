package br.com.geac.backend.domain.exceptions;

public class TagNotFoundException extends BadRequestException {
    public TagNotFoundException(String message) {
        super(message);
    }
}
