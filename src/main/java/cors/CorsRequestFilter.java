package cors;

import java.io.IOException;
import java.util.logging.Logger;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

//Comment out these two annotations below to disable CORS-handling
@Provider  //This will ensure that the filter is used "automatically"
@PreMatching
public class CorsRequestFilter implements ContainerRequestFilter {
  private final static Logger log = Logger.getLogger(CorsRequestFilter.class.getName());
  @Override
  public void filter(ContainerRequestContext requestCtx) throws IOException {
    // When HttpMethod comes as OPTIONS, just acknowledge that it accepts...
    if (requestCtx.getRequest().getMethod().equals("OPTIONS")) {
      // log.info("HTTP Method (OPTIONS) - Detected!");
      // Just send a OK response back to the browser.
      // The response goes through the chain of applicable response filters.
      requestCtx.abortWith(Response.status(Response.Status.OK).build());
    }
  }
} 