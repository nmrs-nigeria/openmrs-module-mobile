package pbsprocessor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DbConnector {
    private Connection con;
    private String dbname, username, password, port;

    public Connection dbDriver() {
        // Get the database connection parameters from the app.properties file which should be in the root of the application
        Properties prop = new Properties();
        String configFile = "app.properties";
        try (FileInputStream fis = new FileInputStream(configFile)) {
            prop.load(fis);
            dbname = prop.getProperty("app.dbname");
            username = prop.getProperty("app.username");
            password = prop.getProperty("app.password");
            port = prop.getProperty("app.port");
        } catch (FileNotFoundException e) {
            System.out.println("CIHP-0001 Error: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("CIHP-0002 Error: " + e.getMessage());
        }

        try {
         Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://localhost:" + port + "/" + dbname, username, password);

        } catch (Exception e) {
            System.out.println(e);
        }
        return con;
    }
}
