package pbsprocessor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class OpenMRS {
    private OpenMRS(){
        Properties prop = new Properties();
        String configFile = "app.properties";
        try (FileInputStream fis = new FileInputStream(configFile)) {
            prop.load(fis);
            user = prop.getProperty("app.openmrs.user");
            password = prop.getProperty("app.openmrs.password");
            baseIP = prop.getProperty("app.server.ip");
            pbsPort = prop.getProperty("app.pbs.server.port");
            openmrsPort = prop.getProperty("app.openmrs.server.port");
            openmrsLink = prop.getProperty("app.openmrs.server.link");
        } catch (FileNotFoundException e) {
            System.out.println("CIHP-0001 Error: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("CIHP-0002 Error: " + e.getMessage());
        }
    }
    private String  openmrsPort;
    private String  openmrsLink;
    private   static  OpenMRS instance;
   public static OpenMRS   getInstance(){
        if(instance==null)
            instance = new OpenMRS();
        return  instance;
    }
    private String protocol="http";
//http://172.19.2.123:2018/api/FingerPrint/SaveToDatabase
//http://172.19.2.123:2018/api/FingerPrint/SaveToDatabase
//22-08-2023 10:46:06:  URL C     http://172.19.2.123:2018/api/FingerPrint/ReSaveFingerprintVerificationToDatabase

    public String getUrlBase(){
        return protocol+"://"+ baseIP+":"+openmrsPort+"/"+openmrsLink;
    }
    public String getUrlCapture(){
        return protocol+"://"+ baseIP+":"+pbsPort+"/api/FingerPrint/SaveToDatabase";
    }
    public String getUrlRecature(){
        return protocol+"://"+ baseIP+":"+pbsPort+"/api/FingerPrint/ReSaveFingerprintVerificationToDatabase";
    }
    public String getUrlServerStatus(){
        return protocol+"://"+ baseIP+":"+pbsPort+"/server";
    }


   private String baseIP="127.0.0.1";
  private String pbsPort = "2018";

 private    String user="admin";

    public String getOpenmrsPort() {
        return openmrsPort;
    }

    public void setOpenmrsPort(String openmrsPort) {
        this.openmrsPort = openmrsPort;
    }

    public String getOpenmrsLink() {
        return openmrsLink;
    }

    public void setOpenmrsLink(String openmrsLink) {
        this.openmrsLink = openmrsLink;
    }

    public String getPbsPort() {
        return pbsPort;
    }

    public void setPbsPort(String pbsPort) {
        this.pbsPort = pbsPort;
    }

    private   String password= "Admin123";


    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getBaseIP() {
        return baseIP;
    }

    public void setBaseIP(String baseIP) {
        this.baseIP = baseIP;
    }



    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
