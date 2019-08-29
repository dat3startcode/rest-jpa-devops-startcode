package entityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
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


    public static EntityManagerFactory getEMF(Strategy strategy){
        Map<String, String> props = getProps();
            return createEntityManagerFactory("pu", props.get("connection"), props.get("user"), props.get("password"), strategy);
    }
     
    public static EntityManagerFactory createEntityManagerFactory(
            String puName,
            String connection_str,
            String user,
            String pw,
            Strategy strategy) {

        Properties props = new Properties();
        
        //A test running on a different thread can alter values to use via these properties
        System.out.println("IS Testing: " + System.getProperty("IS_TEST"));
        if (System.getProperty("IS_TEST") != null) {
            connection_str = System.getProperty("IS_TEST");
            user = System.getProperty("USER") != null ? System.getProperty("USER") : user;
            pw = System.getProperty("PW") != null ? System.getProperty("PW") : pw;
        }
        
        //A deployment server MUST set the following values which will override the defaults

        System.out.println("IS DEPLOYED " + System.getenv("DEPLOYED"));
        boolean isDeployed = (System.getenv("DEPLOYED") != null);
        if (isDeployed) {
            user = System.getenv("USER");
            pw = System.getenv("PW");
            connection_str = System.getenv("CONNECTION_STR");
        }
        /*
        On your server in /opt/tomcat/bin/setenv.sh   add the following WITH YOUR OWN VALUES
        
        export DEPLOYED="DEV_ON_DIGITAL_OCEAN"
        export USER="dev"
        export PW="ax2"
        export CONNECTION_STR="jdbc:mysql://localhost:3306/mydb"
        
        Then save the file, and restart tomcat: sudo systemctl restart tomcat
        */
        
        System.out.println("PU_NAME ------------> "+puName);
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

    
    public static Map<String,String> getProps(){
        Map<String, String> props = new HashMap();
        try (InputStream input = EMF_Creator.class.getClassLoader().getResourceAsStream("config.properties")) {
            Properties config = new Properties();
            config.load(input);

            props.put("server",config.getProperty("db.url"));
            props.put("port",config.getProperty("db.port"));
            props.put("user",config.getProperty("db.user"));
            props.put("password",config.getProperty("db.password"));
            props.put("database",config.getProperty("db.db"));
            props.put("test_port",config.getProperty("test.port"));
            props.put("test_server",config.getProperty("test.server"));
            props.put("connection","jdbc:mysql://"+config.getProperty("db.server")+":"+config.getProperty("db.port")+"/"+config.getProperty("db.database"));
            
            } catch (IOException ex) {
            ex.printStackTrace();
        }
        return props;
    }
    public static void main(String[] args) {
        try {
            System.out.println(getProps().get("connection"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }  
}