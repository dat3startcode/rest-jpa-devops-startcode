package errorhandling;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    @Context
    ServletContext context;

    @Override
    public Response toResponse(Throwable ex) {
        Logger.getLogger(GenericExceptionMapper.class.getName()).log(Level.SEVERE, null, ex);
        Response.StatusType type = getStatusType(ex);
        ExceptionDTO err;
        if (ex instanceof WebApplicationException) {
            err = new ExceptionDTO(type.getStatusCode(), ((WebApplicationException) ex).getMessage());
        } else {

            err = new ExceptionDTO(type.getStatusCode(), type.getReasonPhrase());
        }
        return Response.status(type.getStatusCode())
                .entity(gson.toJson(err))
                .type(MediaType.APPLICATION_JSON).
                build();
    }

    private Response.StatusType getStatusType(Throwable ex) {
        if (ex instanceof WebApplicationException) {
            return ((WebApplicationException) ex).getResponse().getStatusInfo();
        }
        return Response.Status.INTERNAL_SERVER_ERROR;
    }
    //Small hack, to provide json-error response in the filter
    public static Response makeErrRes(String msg,int status){
        ExceptionDTO error = new ExceptionDTO(status, msg);
        String errJson =gson.toJson(error); 
        return Response.status(error.getCode())
                .entity(errJson)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
