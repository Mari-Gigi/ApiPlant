/*package com.svalero.ApiPlant.exception;

import com.svalero.ApiPlant.domain.Cuidado;

public class CuidadoConflictException extends Exception {

    public CuidadoConflictException() {super ("plant-associated care");
    }

    public CuidadoConflictException(String message){
        super (message);
    }


}*/


package com.svalero.ApiPlant.exception;

public class CuidadoConflictException extends RuntimeException {

    public CuidadoConflictException() {
        super ("plant-associated care");
    }

    public CuidadoConflictException(String message) {
        super(message);
    }
}
