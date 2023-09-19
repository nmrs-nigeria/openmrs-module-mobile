/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXML2.java to edit this template
 */
package pbsexport;

import SecuGen.FDxSDKPro.jni.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.DirectoryChooser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.python.core.PyInteger;
import org.python.core.PyString;
import org.python.util.PythonInterpreter;

/**
 * @author Udanwojo David
 */
public class FXMLDocumentController extends Thread implements Initializable {

    @FXML
    private Label label;

    @FXML
    private TextArea messageLog, duplicate_print_display;

    @FXML
    private Button testBtn;

    private JSGFPLib fplib;
    private final String templateThumb1 = "Rk1SACAyMAAAAACWAAABBAEsAMUAxQEAARBeFEAVADflAECJAD3hAEAoAD1pAEB2AD5dAEARAEVlAEBUAFVeAIDKAFdfAICqAGBlAEAQAGBuAEB4AH9pAEArAIZsAEBDAJdxAIB7AKtsAIDwAK5nAEBBALtzAECxAMRkAEARAMqAAEDhAO1pAECqAPVoAEByAR1uAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";
    private final String templateThumb2 = "Rk1SACAyMAAAAACuAAABBAEsAMUAxQEAARBgGEA5ABZsAEAjABvxAEBgACNiAIBZACdoAECTAEboAECCAElhAEAfAEvsAEA0AFBsAEAdAFlsAIDYAFpiAEBiAGJiAIC6AGZoAEAfAHRxAECLAIluAIBEAJZvAEBXAKdvAECQALBxAIASALSHAEBYAMl1AEDKAMpoAEApANqGAEAaANwGAEDGAPpuAEAxAROGAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";
    private final String templateFore = "Rk1SACAyMAAAAAD2AAABBAEsAMUAxQEAARBgJEBhACIDAEBCACcRAEB4ACmAAEC3ACtnAECjADpoAEAmADuYAEBQAD6WAEDQAD/kAEB4AEWHAEAwAEcVAEBeAEmQAEBnAEoOAEBNAFETAIBOAGYOAECpAGxxAECGAHH4AEDkAHtlAIDLAH9sAEB+AJeDAEDqAJ1sAECUAKX0AEAVAKwNAIA3AK2MAEC1ALJxAEAYALoNAEDBAMLoAIChAMPvAEC4AMpuAEB4AMyKAICCAPGJAIBdAPYRAECZAQ3bAICQARFzAEB3ARMGAIBsAR4GAECEASCAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";
    private final String templateFromDB1 = "Rk1SACAyMAAAAADwAAABBAEsAMUAxQEABRBJI4DGAA31AECZACYDAIC9ADRyAIBoAD+eAEC0AEDoAIBDAFEcAICnAFPkAIDDAF/UAEChAGDGAIDUAGfJAICbAGi4AIDpAGnMAED8AHXKAECgAHu1AECqAH6zAEB/AH6sAID3AH9MAEChAIs1AIDfAI/AAICOAJEwAICAAJirAEC5AJm4AECZAJyzAIBsAKElAICYALcvAEDNALq7AIDDAMW2AIA4ANMVAIDTANbAAEC2AOkmAEC9APQVAEBWAPUbAECwARMGAEBkARQbAECeARYVAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";
    private final String templateFromDB2 = "Rk1SACAyMAAAAAD8AAABBAEsAMUAxQEAARBbJYCMADkKAIAgAFolAIBwAGQUAICvAHCAAIA5AHwmAEDVAH7nAIBLAJUlAEDJAJzjAIB3AKUOAICRAKwHAIAOAK0vAIBUAK0fAED1ALFdAIB9ALKRAEBtALgQAIAaAMCxAECMAMf7AIAbAM8zAEC6ANVhAEC/ANnWAIBhANseAIC1AN3NAIBAAN+uAIDJAOnNAEA0AO4oAIBXAPEoAECGAPasAICTAPqyAIDNAP/NAIDdAQFuAICFAQkpAEDdARGQAEArARcmAEDiARyRAEDRAR0QAECpASImAIBmASMYAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA==";
    HashMap<String, String> arrayListTemplatesDB = new HashMap<String, String>();
    private long iError;
    volatile List<String> fingerPrints = Collections.synchronizedList(new ArrayList<>());
    List<Integer> foundDuplicateIds = new ArrayList<>();
    long iErrorM;

    @FXML
    private void handleButtonAction(ActionEvent event) {
        PythonInterpreter pi = new PythonInterpreter();
        pi.set("integer", new PyInteger(42));
        pi.exec("square = integer*integer");
        PyInteger square = (PyInteger) pi.get("square");

        pi.set("solution", new PyString("Baron"));
        pi.exec("solution");
        PyString solution = (PyString) pi.get("solution");
        System.out.println("You clicked me!");
        //this.connect();
        this.matchFingers();
        this.getFileSystem();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    }

    public void connect() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/openmrs", "root", "Admin123");
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("select * from biometricinfo");
            while (rs.next()) {
                System.out.println(rs.getInt(1) + "  " + rs.getString(2) + "  " + rs.getString(3));
            }
            label.setText("Loaded Finger Prints into File");
            con.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void matchFingers() {
        if (fplib == null) {
            this.initializeDevice();
        }
        int[] matchScore = new int[1];
        boolean[] matched = new boolean[1];
        long iError;
        matched[0] = false;

        byte[] newtemplateThumb1 = Base64.getDecoder().decode(this.templateThumb1);
        byte[] newtemplateThumb2 = Base64.getDecoder().decode(this.templateThumb2);

        iError = fplib.MatchIsoTemplate(newtemplateThumb2, 0, newtemplateThumb1, 0, SGFDxSecurityLevel.SL_ABOVE_NORMAL, matched);
        if (iError == SGFDxErrorCode.SGFDX_ERROR_NONE) {
            matchScore[0] = 0;
            iError = fplib.GetIsoMatchingScore(newtemplateThumb1, 0, newtemplateThumb2, 0, matchScore);

            if (iError == SGFDxErrorCode.SGFDX_ERROR_NONE) {
                if (matched[0]) {
                    System.out.println("Registration Success, Matching Score: " + matchScore[0]);
                } else {
                    System.out.println("Registration Fail, Matching Score: " + matchScore[0]);
                }

            } else {
                System.out.println("Registration Fail, GetMatchingScore() Error : " + iError);
            }
        } else {
            System.out.println("Registration Fail, MatchTemplate() Error : " + iError + " " + SGFDxErrorCode.SGFDX_ERROR_NONE);
        }
    }

    private void initializeDevice() {
        fplib = new JSGFPLib();
        if (fplib.Init(SGFDxDeviceName.SG_DEV_AUTO) == SGFDxErrorCode.SGFDX_ERROR_NONE) {
            fplib.SetTemplateFormat(SGFDxTemplateFormat.TEMPLATE_FORMAT_ISO19794);
        }

    }

    private void getFileSystem() {
        String userDir = new File(System.getProperty("user.dir")).getAbsolutePath();
        System.out.println(userDir);
    }

    @FXML
    private void ChoosePBSFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(PBSexport.st);

        if (selectedDirectory == null) {
            //No Directory selected
        } else {
            //stopp = false;          // Start running upDis thread

            DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a");
            Date processing_start_timestamp = new Date();

            // Get the current timestamp when the processing started
            this.messageLog.setText("Scanning started @ " + dateFormat.format(processing_start_timestamp) + "\r\n\n");
            File folder = new File(selectedDirectory.getAbsolutePath());
            File[] listOfFiles = folder.listFiles();
            int processed_prints = 0;
            for (File listOfFile : listOfFiles) {
                if (listOfFile.isFile()) {
                    //System.out.println("File " + listOfFiles[i].getName());
                    //JSON parser object to parse read file
                    JSONParser jsonParser = new JSONParser();
                    try (final FileReader reader = new FileReader(selectedDirectory.getAbsolutePath() + "/" + listOfFile.getName())) {
                        //Read JSON file
                        Object obj = jsonParser.parse(reader);

                        JSONArray patientList = (JSONArray) obj;
                        //Iterate over employee array
                        //patientList.forEach(patients -> processPatients((JSONObject) patients));
                        for (int i = 0; i < patientList.size(); i++) {
                            processed_prints++;
                            JSONObject patientObject = (JSONObject) patientList.get(i);
                            processPatients((JSONObject) patientObject);
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (org.json.simple.parser.ParseException e) {
                        e.printStackTrace();
                    }
                } else if (listOfFile.isDirectory()) {
                    //System.out.println("Directory " + listOfFiles[i].getName());
                }
            }
            
            // Get the current timestamp when the processing finished
            Date processing_end_timestamp = new Date();
            String output_message = "\r\nCompleted @ " + dateFormat.format(processing_end_timestamp) + " : A total of " + processed_prints + " fingerprints were updated successfully" + "\r\n";
            this.messageLog.appendText(output_message);
            System.out.println(output_message);
        }
    }

    public void processPatients(JSONObject patient) {
        JSONObject patientObject = (JSONObject) patient;
        //Access the templates loaded into file and loop to check for duplicates
        String patient_uuid = (String) patientObject.get("patient_id");
        String fingerPosition = (String) patientObject.get("fingerPosition");
        String imageWidth = (String) patientObject.get("imageWidth");
        String imageHeight = (String) patientObject.get("imageHeight");
        String imageDPI = (String) patientObject.get("imageDPI");
        String imageQuality = (String) patientObject.get("imageQuality");
        String serialNumber = (String) patientObject.get("serialNumber");
        String model = "";
        String manufacturer = "";

        String templateFile = (String) patientObject.get("template");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();

        String date_created = dateFormat.format(date);

        int patientId = this.GetPatientIDFromUUID(patient_uuid);
        if (patientId != 0) {
            System.out.println("Inserting " + fingerPosition);
            //check if patient already exists
            JavaDBConnect jdbc = new JavaDBConnect();
            Connection con = jdbc.JavaDBConnectToDB();
            try {
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT biometricInfo_Id from biometricinfo WHERE patient_Id = " + patientId + " AND fingerPosition = '" + fingerPosition + "'");
                //If the patient has an exisiting biometrics then delete and update else just update
                if (rs.next()) {
                    //Delete the column
                    String biometricInfo_Id = rs.getString("biometricInfo_Id");
                    stmt.executeUpdate("DELETE from biometricinfo WHERE biometricInfo_Id = " + biometricInfo_Id);
                    //Then start inserting new ones
                    String insertStatement = "INSERT INTO biometricinfo (patient_Id, fingerPosition, new_template, imageWidth, imageHeight, imageDPI, imageQuality, serialNumber, creator, date_created) VALUES ('" + patientId + "', '" + fingerPosition + "', '" + templateFile + "', '" + imageWidth + "', '" + imageHeight + "', '" + imageDPI + "', '" + imageQuality + "', '" + serialNumber + "', 0, '" + date_created + "')";
                    stmt.execute(insertStatement, Statement.RETURN_GENERATED_KEYS);
                    this.messageLog.appendText("Updating existing Fingerprint" + "\r\n");
                } else {
                    String insertStatement = "INSERT INTO biometricinfo (patient_Id, fingerPosition, new_template, imageWidth, imageHeight, imageDPI, imageQuality, serialNumber, creator, date_created) VALUES ('" + patientId + "', '" + fingerPosition + "', '" + templateFile + "', '" + imageWidth + "', '" + imageHeight + "', '" + imageDPI + "', '" + imageQuality + "', '" + serialNumber + "', 0, '" + date_created + "')";
                    stmt.execute(insertStatement, Statement.RETURN_GENERATED_KEYS);
                    System.out.println("Creating new Fingerprint record");
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                System.out.println("Completed " + patientId + " " + fingerPosition);
                this.messageLog.appendText("Fingerprints uploaded successfully" + "\r\n");
                try {
                    con.close();
                } catch (SQLException e) {
                    System.out.println("[CIHP-0003] Error closing DB connect => " + e.getMessage());
                }
            }
        } else {
            // Show Patients that are not found in the Database
            this.messageLog.appendText("patient_uuid " + patient_uuid + " not found.\r\n");
        }

    }
    
    private int GetPatientIDFromUUID(String patient_Uuid) {
        JavaDBConnect jdbc = new JavaDBConnect();
        Connection con = jdbc.JavaDBConnectToDB();
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT person_id from person WHERE uuid = '" + patient_Uuid + "'");
            if (rs.next()) {
                String patient_Id = rs.getString("person_id");
                return Integer.parseInt(patient_Id);
            }
            return 0;
        } catch (NumberFormatException | SQLException e) {
            System.out.println(e.getMessage());
        }
        finally {
            try {
                con.close();
            } catch (SQLException e) {
                System.out.println("[CIHP-0001] Error closing DB connect => " + e.getMessage());
            }
        }
        return 0;
    }

    public void customCheckDuplicates(byte[] unknownTemplate, Integer patientId) {
        if (!this.fingerPrints.isEmpty()) {
            if (fplib == null) {
                this.initializeDevice();
                System.out.println("Value is: " + fplib.Init(SGFDxDeviceName.SG_DEV_AUTO));
            }
            int sumAll = 0;

            try {
                boolean[] matched = new boolean[1];
                matched[0] = false;
                int[] matchScore = new int[1];

                ExecutorService taskExecutor;

                taskExecutor = Executors.newFixedThreadPool(10);

                //Executors.newSingleThreadExecutor().execute( task );
                int counter = 0;
                for (String singlePrint : this.fingerPrints) {
                    //If this patient id already exisits in the duplicate list the break out of the loop and continue onto the next one
                    if (foundDuplicateIds.contains(patientId)) {
                        continue;
                    }
                    if (fplib == null) {
                        this.initializeDevice();
                    }
                    sumAll++;
                    Thread thread = new Thread(() -> {

                        byte[] fingerTemplate = Base64.getDecoder().decode(singlePrint);

                        long iErrorM = fplib.MatchIsoTemplate(fingerTemplate, 0, unknownTemplate, 0, SGFDxSecurityLevel.SL_ABOVE_NORMAL, matched);
                        //iError = fplib.MatchIsoTemplate(newtemplateThumb2, 0, newtemplateThumb1, 0, SGFDxSecurityLevel.SL_ABOVE_NORMAL, matched);
                        if (iErrorM == SGFDxErrorCode.SGFDX_ERROR_NONE) {
                            matchScore[0] = 0;
                            iErrorM = fplib.GetIsoMatchingScore(fingerTemplate, 0, unknownTemplate, 0, matchScore);
                            if (iErrorM == SGFDxErrorCode.SGFDX_ERROR_NONE) {
                                if (matched[0]) {
                                    //If there is a match then add this patient id to the list
                                    foundDuplicateIds.add(patientId);
                                    System.out.println("Registration Success, Matching Score: " + matchScore[0]);
                                } else {
                                    System.out.println("Registration Fail, Matching Score: " + matchScore[0]);
                                }

                            } else {
                                System.out.println("Registration Fail, GetMatchingScore() Error : " + iErrorM);
                            }
                        } else {
                            System.out.println("Registration Fail, MatchTemplate() Error : " + iErrorM + " " + SGFDxErrorCode.SGFDX_ERROR_NONE);
                        }
                    });
                    fplib = null;
                    thread.setName("Thread" + counter++);
                    taskExecutor.submit(thread);
                    System.out.println(sumAll);
                }
                taskExecutor.shutdown();
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                System.out.println(sumAll);
            }
        }

    }

    @FXML
    public void handleTest() {
        byte[] unknownTemplate = Base64.getDecoder().decode(this.templateFromDB1);
        //this.customCheckDuplicates(unknownTemplate);
    }
}