package utils;

import java.util.Properties;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class EMF_Creator {

    public enum Strategy {
        NONE {
            @Override
            public String toString() {
                return "drop-and-create";
            }
        },
        CREATE {
            @Override
            public String toString() {
                return "create";
            }
        },
        DROP_AND_CREATE {
            @Override
            public String toString() {
                return "drop-and-create";
            }
        }
    };
    
    public enum DbSelector {
        DEV {
            @Override
            public String toString() {
                return "db.database";
            }
        },
        TEST {
            @Override
            public String toString() {
                return "dbtest.database";
            }
        }
    }


    /**
     * Call this method before all tests, in integration tests that uses the Grizzly Server and the Test Database
     * (in  @BeforeAll )
     * Remember to call enRestTestWithDB() (in @AfterAll)
     */
    public static void startREST_TestWithDB(){
        System.setProperty("IS_INTEGRATION_TEST_WITH_DB", "testing");
    }

    /*
      Call this method in your @AferAll method if startREST_TestWithDB() was previously called
    */
    public static void endREST_TestWithDB(){
      System.clearProperty("IS_INTEGRATION_TEST_WITH_DB"); 
    }

    /**
     * Create an EntityManagerFactory using values set in 'config.properties'
     * <p>
     * Important: If used from a REST-test call this method before you start the test server
     * </p>
     * @param dbType 
     * @param strategy 
     * @return The new EntityManagerFactory
     */
    public static EntityManagerFactory createEntityManagerFactory(DbSelector dbType,Strategy strategy){
        String puName="pu"; //Only legal name
        String connection_str;
        String user;
        String pw;
        if(dbType == DbSelector.DEV){
            connection_str = Settings.getDEV_DBConnection();
            user = Settings.getPropertyValue("db.user");
            pw = Settings.getPropertyValue("db.password");
            System.clearProperty("IS_TEST");
        } else{          
            connection_str = Settings.getTEST_DBConnection();
            //Will ensure REST code "switches" to this DB, even when running on a separate JVM
            System.setProperty("IS_TEST", connection_str);
            user = Settings.getPropertyValue("dbtest.user")!= null ? Settings.getPropertyValue("dbtest.user") : Settings.getPropertyValue("db.user") ;
            pw = Settings.getPropertyValue("dbtest.password")!= null ? Settings.getPropertyValue("dbtest.password") : Settings.getPropertyValue("db.password") ;
        }
        return createEntityManagerFactory(puName,connection_str,user,pw,strategy);
    }
    /**
     * Create an EntityManagerFactory using the supplied values
     * @param puName
     * @param connection_str
     * @param user
     * @param pw
     * @param strategy
     * @return  The new EntityManagerFactory
     */        
    public static EntityManagerFactory createEntityManagerFactory(
            String puName,
            String connection_str,
            String user,
            String pw,
            Strategy strategy) {

        Properties props = new Properties();

        //A test running on a different JVM can alter values to use via this property
        if (System.getProperty("IS_INTEGRATION_TEST_WITH_DB") != null) {
            System.out.println("--------- IS_INTEGRATION_TEST_WITH_DB (Integration Test With DataBase --------------- ");
            connection_str = Settings.getTEST_DBConnection();
            user = System.getProperty("USER") != null ? System.getProperty("USER") : user;
            pw = System.getProperty("PW") != null ? System.getProperty("PW") : pw;
        }
        
        //A deployment server MUST set the following values which will override the defaults
        boolean isDeployed = (System.getenv("DEPLOYED") != null);      
        if (isDeployed) {
            String remoteUserKey = Settings.getPropertyValue("remote.db.user.key");
            String remotePwKey = Settings.getPropertyValue("remote.db.password.key");
            String remoteConnectionKey = Settings.getPropertyValue("remote.db.connection.key");
            user = System.getenv(remoteUserKey);
            pw = System.getenv(remotePwKey);
            connection_str = System.getenv(remoteConnectionKey);
        }

        /*
        On your server in /opt/tomcat/bin/setenv.sh   add the following WITH YOUR OWN VALUES and your own
        keys, defined in config.properties

        export DEPLOYED="DEV_ON_DIGITAL_OCEAN"
        export USER="dev"
        export PW="ax2"
        export CONNECTION_STR="jdbc:mysql://localhost:3306/mydb"
        
        Then save the file, and restart tomcat: sudo systemctl restart tomcat
        */
        
        System.out.println("USER ------------> "+user);
        System.out.println("PW --------------> "+pw);
        System.out.println("CONNECTION STR---> "+connection_str);
        System.out.println("PU-Strategy---> "+strategy.toString());
        
        props.setProperty("javax.persistence.jdbc.user", user);
        props.setProperty("javax.persistence.jdbc.password", pw);
        props.setProperty("javax.persistence.jdbc.url", connection_str);
        if (strategy != Strategy.NONE) {
            props.setProperty("javax.persistence.schema-generation.database.action", strategy.toString());
        }
        return Persistence.createEntityManagerFactory(puName, props);
    }
}
