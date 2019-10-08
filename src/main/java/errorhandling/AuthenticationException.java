package errorhandling;

/**
 *
 * @author lam@cphbusiness.dk
 */
public class AuthenticationException extends Exception{

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException() {
        super("Could not be Authenticated");
    }  
}
