package com.svalero.ApiPlant.exception;

public class PlantaNotFoundException extends Exception {

    public PlantaNotFoundException() {
        super ();
    }

    public PlantaNotFoundException(String message){
        super (message);
    }
}
