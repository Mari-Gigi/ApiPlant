package com.svalero.ApiPlant.exception;

public class PlagaNotFoundException extends Exception {

    public PlagaNotFoundException() {
        super ("Pest does not exist");
    }

    public PlagaNotFoundException(String message){
        super (message);
    }
}
