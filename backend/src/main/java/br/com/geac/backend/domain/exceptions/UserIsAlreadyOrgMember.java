package br.com.geac.backend.domain.exceptions;

public class UserIsAlreadyOrgMember extends BadRequestException {
    public UserIsAlreadyOrgMember(String message) {super(message);}
}
