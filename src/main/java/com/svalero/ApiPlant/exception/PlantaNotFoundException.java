package com.svalero.ApiPlant.exception;

public class PlantaNotFoundException extends RuntimeException {

    public PlantaNotFoundException() {
        super ();
    }

    public PlantaNotFoundException(String message){
        super (message);
    }
}
