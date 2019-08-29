package facades;

import entityUtils.EMF_Creator;
import entities.RenameMe;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

//Uncomment the line below, to temporarily disable this test
//@Disabled
public class FacadeExampleTest {
    private static  EntityManagerFactory emf;
    private static  FacadeExample facade;
    public FacadeExampleTest() {
    }
    
    @BeforeAll
    public static void setUpClass() {
        //   emf = EMF_Creator.createEntityManagerFactory(
        //         "pu",
        //         "jdbc:mysql://localhost:3307/startcode_test",
        //         "dev",
        //         "ax2",
        //         EMF_Creator.Strategy.DROP_AND_CREATE);
//          emf = EMF_Creator.createEntityManagerFactory(
//                "pu",
//                "jdbc:mysql://localhost:3307/startcodev2-test",
//                "dev",
//                "ax2",
//                EMF_Creator.Strategy.DROP_AND_CREATE);
        emf = EMF_Creator.getEMF(EMF_Creator.Strategy.DROP_AND_CREATE);
        facade = FacadeExample.getFacadeExample(emf);
    }
    
    @AfterAll
    public static void tearDownClass() {
//        Clean up database after test is done or use a persistence unit with drop-and-create to start up clean on every test
    }
    
    // Setup the DataBase in a known state BEFORE EACH TEST
    //TODO -- Make sure to change the script below to use YOUR OWN entity class
    @BeforeEach
    public void setUp() {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.createNamedQuery("RenameMe.deleteAllRows").executeUpdate();
            // em.createQuery("DELETE from RenameMe").executeUpdate();
            em.persist(new RenameMe("Some txt","More text"));
            em.persist(new RenameMe("aaa","bbb"));
           
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
    
    @AfterEach
    public void tearDown() {
//        Remove any data after each test was run
    }

    // TODO: Delete or change this method 
    @Test
    public void testAFacadeMethod() {
        assertEquals(2,facade.getRenameMeCount(),"Expects two rows in the database");
    }
    
}
