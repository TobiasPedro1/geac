package br.com.geac.backend.domain.exceptions;

public class RequirementNotFoundException extends BadRequestException {
    public RequirementNotFoundException(String message) {
        super(message);
    }
}
