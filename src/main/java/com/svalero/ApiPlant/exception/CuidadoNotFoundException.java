package com.svalero.ApiPlant.exception;

public class CuidadoNotFoundException extends Exception {

    public CuidadoNotFoundException() {
        super ("Care does not exist");
    }

    public CuidadoNotFoundException(String message){
        super (message);
    }


}
