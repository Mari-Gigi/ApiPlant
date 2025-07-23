package com.svalero.ApiPlant.exception;

public class ConsejoConflictException extends RuntimeException {

    public ConsejoConflictException() {
        super ("plant-associated advice");
    }

    public ConsejoConflictException(String message) {
        super(message);
    }
}

