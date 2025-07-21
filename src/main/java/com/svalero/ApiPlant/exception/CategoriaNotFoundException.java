package com.svalero.ApiPlant.exception;

public class CategoriaNotFoundException extends Exception {

    public CategoriaNotFoundException() {
        super ("Category does not exist");
    }

    public CategoriaNotFoundException(String message){
        super (message);
    }


}
