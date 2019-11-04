package rest;

import entities.RenameMe;
import entities.User;
import entities.Role;

import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import java.net.URI;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import static org.hamcrest.Matchers.equalTo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import utils.EMF_Creator;

//@Disabled
public class LoginEndpointTest {

    private static final int SERVER_PORT = 7777;
    private static final String SERVER_URL = "http://localhost/api";
    private static RenameMe r1, r2;

    static final URI BASE_URI = UriBuilder.fromUri(SERVER_URL).port(SERVER_PORT).build();
    private static HttpServer httpServer;
    private static EntityManagerFactory emf;

    static HttpServer startServer() {
        ResourceConfig rc = ResourceConfig.forApplication(new ApplicationConfig());
        return GrizzlyHttpServerFactory.createHttpServer(BASE_URI, rc);
    }

    @BeforeAll
    public static void setUpClass() {
        //This method must be called before you request the EntityManagerFactory
        EMF_Creator.startREST_TestWithDB();
        emf = EMF_Creator.createEntityManagerFactory(EMF_Creator.DbSelector.TEST, EMF_Creator.Strategy.CREATE);

        httpServer = startServer();
        //Setup RestAssured
        RestAssured.baseURI = SERVER_URL;
        RestAssured.port = SERVER_PORT;
        RestAssured.defaultParser = Parser.JSON;
    }

    @AfterAll
    public static void closeTestServer() {
        //Don't forget this, if you called its counterpart in @BeforeAll
        EMF_Creator.endREST_TestWithDB();
        httpServer.shutdownNow();
    }

    // Setup the DataBase (used by the test-server and this test) in a known state BEFORE EACH TEST
    //TODO -- Make sure to change the EntityClass used below to use YOUR OWN (renamed) Entity class
    @BeforeEach
    public void setUp() {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            //Delete existing users and roles to get a "fresh" database
            em.createQuery("delete from User").executeUpdate();
            em.createQuery("delete from Role").executeUpdate();

            Role userRole = new Role("user");
            Role adminRole = new Role("admin");
            User user = new User("user", "test");
            user.addRole(userRole);
            User admin = new User("admin", "test");
            admin.addRole(adminRole);
            User both = new User("user_admin", "test");
            both.addRole(userRole);
            both.addRole(adminRole);
            em.persist(userRole);
            em.persist(adminRole);
            em.persist(user);
            em.persist(admin);
            em.persist(both);
            System.out.println("Saved test data to database");
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
    
    //This is how we hold on to the token after login, similar to that a client must store the token somewhere
  private static String securityToken;

  //Utility method to login and set the returned securityToken
  private static void login(String role, String password) {
    String json = String.format("{username: \"%s\", password: \"%s\"}", role, password);
    securityToken = given()
            .contentType("application/json")
            .body(json)
            //.when().post("/api/login")
            .when().post("/login")
            .then()
            .extract().path("token");
      System.out.println("TOKEN ---> "+securityToken);
  }

  private void logOut() {
    securityToken = null;
  }

  @Test
  public void serverIsRunning() {
    System.out.println("Testing is server UP");
    given().when().get("/info").then().statusCode(200);
  }

  @Test
  public void testRestNoAuthenticationRequired() {
    given()
            .contentType("application/json")
            .when()
            .get("/info").then()
            .statusCode(200)
            .body("msg", equalTo("Hello anonymous"));
  }

  @Test
  public void testRestForAdmin() {
    login("admin", "test");
    given()
            .contentType("application/json")
            .accept(ContentType.JSON)
            .header("x-access-token", securityToken)
            .when()
            .get("/info/admin").then()
            .statusCode(200)
            .body("msg", equalTo("Hello to (admin) User: admin"));
  }

  @Test
  public void testRestForUser() {
    login("user", "test");
    given()
            .contentType("application/json")
            .header("x-access-token", securityToken)
            .when()
            .get("/info/user").then()
            .statusCode(200)
            .body("msg", equalTo("Hello to User: user"));
  }
  
  @Test
  public void testAutorizedUserCannotAccesAdminPage() {
    login("user", "test");
    given()
            .contentType("application/json")
            .header("x-access-token", securityToken)
            .when()
            .get("/info/admin").then()  //Call Admin endpoint as user
            .statusCode(401);
  }
  
  @Test
  public void testAutorizedAdminCannotAccesUserPage() {
    login("admin", "test");
    given()
            .contentType("application/json")
            .header("x-access-token", securityToken)
            .when()
            .get("/info/user").then()  //Call User endpoint as Admin
            .statusCode(401);
  }
  
  @Test
  public void testRestForMultiRole1() {
    login("user_admin", "test");
    given()
            .contentType("application/json")
            .accept(ContentType.JSON)
            .header("x-access-token", securityToken)
            .when()
            .get("/info/admin").then()
            .statusCode(200)
            .body("msg", equalTo("Hello to (admin) User: user_admin"));
  }

  @Test
  public void testRestForMultiRole2() {
    login("user_admin", "test");
    given()
            .contentType("application/json")
            .header("x-access-token", securityToken)
            .when()
            .get("/info/user").then()
            .statusCode(200)
            .body("msg", equalTo("Hello to User: user_admin"));
  }

  @Test
  public void userNotAuthenticated() {
    logOut();
    given()
            .contentType("application/json")
            .when()
            .get("/info/user").then()
            .statusCode(403)
            .body("code", equalTo(403))
            .body("message", equalTo("Not authenticated - do login"));
  }

  @Test
  public void adminNotAuthenticated() {
    logOut();
    given()
            .contentType("application/json")
            .when()
            .get("/info/user").then()
            .statusCode(403)
            .body("code", equalTo(403))
            .body("message", equalTo("Not authenticated - do login"));
  }
    
    
}
