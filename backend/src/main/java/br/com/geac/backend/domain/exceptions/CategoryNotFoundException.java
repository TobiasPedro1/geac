package br.com.geac.backend.domain.exceptions;

public class CategoryNotFoundException extends BadRequestException {
    public CategoryNotFoundException(String message) {
        super(message);
    }
}
