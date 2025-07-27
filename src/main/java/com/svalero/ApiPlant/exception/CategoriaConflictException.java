package com.svalero.ApiPlant.exception;

public class CategoriaConflictException extends RuntimeException {

    public CategoriaConflictException() {
        super ();
    }

    public CategoriaConflictException(String message) {
        super(message);
    }
}

