package com.svalero.ApiPlant.exception;

import com.svalero.ApiPlant.domain.Cuidado;

public class PlagaConflictException extends Exception {

    public PlagaConflictException() {super ("plant-associated pest");
    }

    public PlagaConflictException(String message){
        super (message);
    }

}


