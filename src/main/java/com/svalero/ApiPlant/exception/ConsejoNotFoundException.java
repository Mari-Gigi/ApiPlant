package com.svalero.ApiPlant.exception;

public class ConsejoNotFoundException extends Exception {

    public ConsejoNotFoundException() {
        super ("Advice does not exist");
    }

    public ConsejoNotFoundException(String message){
        super (message);
    }
}
