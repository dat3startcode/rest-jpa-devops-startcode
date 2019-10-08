package errorhandling;

/**
 *
 * @author lam@cphbusiness.dk
 */
public class NotFoundException extends Exception{

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException() {
        super("Requested item could not be found");
    }  
}