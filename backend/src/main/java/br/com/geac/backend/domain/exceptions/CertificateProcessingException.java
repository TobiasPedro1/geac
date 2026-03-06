package br.com.geac.backend.domain.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class CertificateProcessingException extends RuntimeException {
    public CertificateProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}