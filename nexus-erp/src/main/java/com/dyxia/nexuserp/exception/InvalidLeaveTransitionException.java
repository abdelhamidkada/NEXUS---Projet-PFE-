package com.dyxia.nexuserp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception lancée lorsqu'une transition d'état de demande de congé est invalide.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidLeaveTransitionException extends RuntimeException {

    public InvalidLeaveTransitionException(String message) {
        super(message);
    }
}
