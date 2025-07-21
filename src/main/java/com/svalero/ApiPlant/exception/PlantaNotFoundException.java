package com.svalero.ApiPlant.exception;

public class PlantaNotFoundException extends Exception {

    public PlantaNotFoundException() {
        super ("The plant does not exist");
    }

    public PlantaNotFoundException(String message){
        super (message);
    }
}
