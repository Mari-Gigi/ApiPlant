package com.svalero.ApiPlant.exception;

public class CategoriaConflictException extends RuntimeException {

    public CategoriaConflictException() {
        super ("plant-associated category");
    }

    public CategoriaConflictException(String message) {
        super(message);
    }
}

