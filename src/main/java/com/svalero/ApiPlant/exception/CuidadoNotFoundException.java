package com.svalero.ApiPlant.exception;

import com.svalero.ApiPlant.domain.Cuidado;

public class CuidadoNotFoundException extends Exception {

    public CuidadoNotFoundException() {
        super ("Care does not exist");
    }

    public CuidadoNotFoundException(String message){
        super (message);
    }


}
