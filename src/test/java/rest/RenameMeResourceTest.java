package rest;

import entities.RenameMe;
import entityUtils.EMF_Creator;
import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;
import io.restassured.parsing.Parser;
import java.net.URI;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import static org.hamcrest.Matchers.equalTo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
public class RenameMeResourceTest {

//    private static final int SERVER_PORT = 7777;
//    private static final String SERVER_URL = "http://localhost/api";
//    //Read this line from a settings-file  since used several places
//    private static final String TEST_DB = "jdbc:mysql://localhost:3307/startcodev2-test";
    private static final Map<String, String> PROPS = entityUtils.EMF_Creator.getProps();
    static final URI BASE_URI = UriBuilder.fromUri(PROPS.get("test_server")).port(Integer.parseInt(PROPS.get("test_port"))).build();
    private static HttpServer httpServer;
    private static EntityManagerFactory emf;

    static HttpServer startServer() {
        System.out.println("BASE_URI: "+BASE_URI);
        ResourceConfig rc = ResourceConfig.forApplication(new ApplicationConfig());
        return GrizzlyHttpServerFactory.createHttpServer(BASE_URI, rc);
    }

    @BeforeAll
    public static void setUpClass() {
        //First Drop and Rebuild the test database 
        emf = EMF_Creator.getEMF(EMF_Creator.Strategy.DROP_AND_CREATE);

        //Set System property so the project executed by the Grizly-server wil use this same database
        System.setProperty("IS_TEST", PROPS.get("connection"));
        //We are using the database on the virtual Vagrant image, so username password are the same for all dev-databases
        
        httpServer = startServer();
        
        //Setup RestAssured
        RestAssured.baseURI = PROPS.get("test_server");
        RestAssured.port = Integer.parseInt(PROPS.get("test_port"));
        
        RestAssured.defaultParser = Parser.JSON;
    }
    
    @AfterAll
    public static void closeTestServer(){
        //System.in.read();
         httpServer.shutdownNow();
    }
    
    // Setup the DataBase (used by the test-server and this test) in a known state BEFORE EACH TEST
    //TODO -- Make sure to change the script below to use YOUR OWN entity class
    @BeforeEach
    public void setUp() {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.createNamedQuery("RenameMe.deleteAllRows").executeUpdate();

            em.persist(new RenameMe("Some txt","More text"));
            em.persist(new RenameMe("aaa","bbb"));
           
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
    
    @Test
    public void serverIsRunning() {
        System.out.println("Testing is server UP");
        given().when().get("/xxx").then().statusCode(200);
    }
   
    //This test assumes the database contains two rows
    @Test
    public void testDummyMsg() throws Exception {
        given()
        .contentType("application/json")
        .get("/xxx/").then()
        .assertThat()
        .statusCode(HttpStatus.OK_200.getStatusCode())
        .body("msg", equalTo("Hello World"));   
    }
    
    @Test
    public void testCount() throws Exception {
        given()
        .contentType("application/json")
        .get("/xxx/count").then()
        .assertThat()
        .statusCode(HttpStatus.OK_200.getStatusCode())
        .body("count", equalTo(2));   
    }
    
    

}
