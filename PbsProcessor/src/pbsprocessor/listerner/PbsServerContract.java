package pbsprocessor.listerner;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class PbsServerContract implements Serializable {

    @SerializedName("databaseServer")
    @Expose
    private String databaseServer;

    @SerializedName("username")
    @Expose
    private String username;

    @SerializedName("password")
    @Expose
    private String password;

    @SerializedName("port")
    @Expose
    private String port;

    @SerializedName("dBName")
    @Expose
    private String dBName;

    @SerializedName("dbPort")
    @Expose
    private String dbPort;

    @SerializedName("appVersion")
    @Expose
    private String appVersion;

    public PbsServerContract(String databaseServer, String username, String password, String port, String dBName, String dbPort, String appVersion) {
        this.databaseServer = databaseServer;
        this.username = username;
        this.password = password;
        this.port = port;
        this.dBName = dBName;
        this.dbPort = dbPort;
        this.appVersion = appVersion;
    }

    public String getDatabaseServer() {
        return databaseServer;
    }

    public void setDatabaseServer(String databaseServer) {
        this.databaseServer = databaseServer;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getDBName() {
        return dBName;
    }

    public void setDBName(String dBName) {
        this.dBName = dBName;
    }

    public String getDBPort() {
        return dbPort;
    }

    public void setDBPort(String dbPort) {
        this.dbPort = dbPort;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    @Override
    public String toString() {
        return "DatabaseConfig{" +
                "databaseServer='" + databaseServer + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", port='" + port + '\'' +
                ", dBName='" + dBName + '\'' +
                ", dbPort='" + dbPort + '\'' +
                ", appVersion='" + appVersion + '\'' +
                '}';
    }
}
