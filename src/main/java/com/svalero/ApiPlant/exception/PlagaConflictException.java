package com.svalero.ApiPlant.exception;

import com.svalero.ApiPlant.domain.Cuidado;

public class PlagaConflictException extends Exception {

    public PlagaConflictException() {super ();
    }

    public PlagaConflictException(String message){
        super (message);
    }

}


