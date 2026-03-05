package br.com.geac.backend.domain.exceptions;

public class UserAlreadySubscribedInEvent extends BadRequestException {
    public UserAlreadySubscribedInEvent(String message) {
        super(message);
    }
}
