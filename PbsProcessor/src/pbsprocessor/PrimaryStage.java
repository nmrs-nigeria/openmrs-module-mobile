package pbsprocessor;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import pbsprocessor.listerner.ProgressListener;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

public class PrimaryStage extends Thread implements Initializable {
    @FXML
    private TextArea show_fingerprint_feedback;

    @FXML
    private Button btn_upload_fingerprint;

    @FXML
    private ProgressBar progressStatusBar;

    File selectedDirectory = null;
    private String fingerprint_processed_feedback = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    // An event handler to minimize the primary windows
    @FXML
    public void minimizeWindow(MouseEvent evt) {
        pbsprocessor.primary_stage = (Stage) ((Button) evt.getSource()).getScene().getWindow();
        pbsprocessor.primary_stage.setIconified(true);
    }

    // An event handler to close the primary windows
    @FXML
    public void closeWindow(MouseEvent evt) {
        pbsprocessor.primary_stage = (Stage) ((Button) evt.getSource()).getScene().getWindow();
        pbsprocessor.primary_stage.close();
    }

    // Open the Standard Operating Procedure using the associated application in this case an Adobe acrobat reader for PDF document
    @FXML
    public void openSOP() throws IOException {
        final Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.OPEN)) {
            // The folder docs/sop.pdf structure MUST also be maintained in the artifact setup
            desktop.open(new File("docs/sop.pdf"));
        } else {
            throw new UnsupportedOperationException("Open action not supported");
        }
    }

    @FXML
    private void pbsFolderLocator() {
        show_fingerprint_feedback.clear();                    // Clear display control before running task

        DirectoryChooser directoryChooser = new DirectoryChooser();
        selectedDirectory = directoryChooser.showDialog(pbsprocessor.primary_stage);

        DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a");
        Date processing_start_timestamp = new Date();

        // Ensure the user selects the right directory
        if (selectedDirectory != null) {
            // Get the current timestamp when the processing started and display it on the GUI
            fingerprint_processed_feedback = "PBS file(s) processing started @ " + dateFormat.format(processing_start_timestamp) + ".\n\n";
            show_fingerprint_feedback.setText(fingerprint_processed_feedback);

            btn_upload_fingerprint.setDisable(true);            // Disable the upload button when processing starts

            PrimaryStage.BackgroundTask dWrk = new PrimaryStage.BackgroundTask();
            dWrk.valueProperty().addListener((observable, oldValue, newValue) -> show_fingerprint_feedback.appendText(String.valueOf(newValue)));
            progressStatusBar.progressProperty().bind(dWrk.progressProperty());

            Thread thread = new Thread(dWrk);
            thread.setDaemon(true);
            thread.start();
        }
    }

    private class BackgroundTask extends Task<String> {
        @Override
        protected String call() throws Exception {

            File folder = new File(selectedDirectory.getAbsolutePath());
            File[] listOfFiles = folder.listFiles();

            /*
             *   Because there may be one or more files to be process; and we don't have know how many records are in each file,
             *   we will have to read the size of the elements in each file, sum them up to know the total number of records to
             *   process. This is necessary because we need to show a progress bar of what has been processed.
             * */


            int total_record_to_process = 0; // Total number of records to process from N number of file
            final int[] patientProgress = new int[1];
           int fileIndex=0;
            System.out.println("Background task started  " );
            for (File listOfFile : listOfFiles) {
                System.out.println("Background task started file index " +fileIndex);
                if (listOfFile.isFile()) {
                    JSONParser jsonParser = new JSONParser();
                    try (final FileReader reader = new FileReader(selectedDirectory.getAbsolutePath() + "/" + listOfFile.getName())) {
                        JSONObject obj = (JSONObject) jsonParser.parse(reader);
                        JSONArray patientList = (JSONArray) obj.get("data");
                        total_record_to_process += patientList.size();
                        System.out.println("patientList.size(): " + patientList.size());
                    } catch (FileNotFoundException e) {
                        Platform.runLater(() -> {  updateValue("File Error " +e.getMessage() );  });
                        System.out.println("[CIHP-0006] Error: " + e.getMessage());
                    } catch (IOException e) {
                        System.out.println("[CIHP-0007] Error: " + e.getMessage());
                        Platform.runLater(() -> {  updateValue("IOk Error " +e.getMessage() );  });
                    } catch (Exception e) {
                        Platform.runLater(() -> {  updateValue("Error " +e.getMessage() );  });
                        System.out.println("[CIHP-0008] Error: " + e.getMessage());
                    }
                } else if (listOfFile.isDirectory()) {
                    Platform.runLater(() -> {  updateValue("Sub file is directory and will not be process " +listOfFile.getName());  });
                    System.out.println("Sub file is directory and will not be process"  );
                    //Todo
                }
            }
            System.out.println("total_patients_to_process: " + total_record_to_process);


            /// syncing the data
            for (File listOfFile : listOfFiles) {
                if (listOfFile.isFile()) {
                    int finalTotal_record_to_process = total_record_to_process;
                    new ImportPBS().startUpload(selectedDirectory.getAbsolutePath() + "/" + listOfFile.getName(),
                            new ProgressListener() {
                                @Override
                                public void onError(String errorMessage) {
                                    Platform.runLater(() -> updateValue(errorMessage+"\n"));
                                    System.out.println(errorMessage);
                                }

                                @Override
                                public void onProgress(int progress, int size, String displayMessage, String errorMessage) {
                                    System.out.println("P " + progress + " M " + displayMessage + " E " + errorMessage);
                                    patientProgress[0] = patientProgress[0] + 1;

                                    Platform.runLater(() -> {
                                        updateValue("Patient " + progress + " " + displayMessage + "\n" + errorMessage);
                                        updateProgress(patientProgress[0], finalTotal_record_to_process);
                                    });

                                }

                                @Override
                                public void stop() {
                                    System.out.println("Stop");
                                    Platform.runLater(() -> updateValue(listOfFile.getName() + " Done \n"));
                                    Platform.runLater(() ->  updateProgress(patientProgress[0], finalTotal_record_to_process));

                                }

                                @Override
                                public void onStart(int size) {
                                    System.out.println("Size " + size);
                                    Platform.runLater(() -> updateValue("Started " + listOfFile.getName() + ". Size " + size + " patient" + (size > 1 ? "s" : "") + " found\n"));

                                }

                                @Override
                                public void serverOkay(boolean isRunning) {
                                    Platform.runLater(() -> updateValue("Server is running  " + isRunning + "\n"));
                                    System.out.println("Server is running  " + isRunning);
                                }
                            }
                    );

                    System.out.print(" Done processing  ");

                } else if (listOfFile.isDirectory()) {
                    //Todo
                }
            }
            System.out.println("total_record_to_process: " + total_record_to_process);
            // Get the current timestamp when the processing finished and display it on the GUI
            DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a");
            Date processing_end_timestamp = new Date();
            String half_feedback = null;
            if (total_record_to_process != 0) {
                half_feedback = "A total of " + patientProgress[0] + " fingerprints were updated and/or created successfully out of"+total_record_to_process+".\n";
            } else {
                half_feedback = "No fingerprints were processed because no PBS exported file(s) were found in the specified directory.\n";
            }
            String output_message = "\r\nCompleted @ " + dateFormat.format(processing_end_timestamp) + ": " + half_feedback;
            fingerprint_processed_feedback = output_message;
            System.out.println(fingerprint_processed_feedback);

            btn_upload_fingerprint.setDisable(false);
            return fingerprint_processed_feedback;


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
        //check if patient already exists
        if (patientId != 0) {
            fingerprint_processed_feedback = "Inserting fingerprint for patient with ID - " + patientId + " - [Finger position is " + fingerPosition + "]\n";
            //System.out.println(fingerprint_processed_feedback);
            DbConnector db_driver = new DbConnector() ;
            Connection con = db_driver.dbDriver();
            try {
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT biometricInfo_Id from biometricinfo WHERE patient_Id = " + patientId + " AND fingerPosition = '" + fingerPosition + "'");
                //If the patient has an exisiting biometrics then delete and update else just update
                String insertStatement = "INSERT INTO biometricinfo (patient_Id, fingerPosition, new_template, imageWidth, imageHeight, imageDPI, imageQuality, serialNumber, creator, date_created) VALUES ('" + patientId + "', '" + fingerPosition + "', '" + templateFile + "', '" + imageWidth + "', '" + imageHeight + "', '" + imageDPI + "', '" + imageQuality + "', '" + serialNumber + "', 0, '" + date_created + "')";
                if (rs.next()) {
                    //Delete the column
                    String biometricInfo_Id = rs.getString("biometricInfo_Id");
                    stmt.executeUpdate("DELETE from biometricinfo WHERE biometricInfo_Id = " + biometricInfo_Id);
                    //Then start inserting new ones
                    stmt.execute(insertStatement, Statement.RETURN_GENERATED_KEYS);
                    fingerprint_processed_feedback = "Updating existing fingerprint for client with ID - " + patientId + " - [Finger position is " + fingerPosition + "]\n";
                    //System.out.println(fingerprint_processed_feedback);
                } else {
                    stmt.execute(insertStatement, Statement.RETURN_GENERATED_KEYS);
                    fingerprint_processed_feedback = "Creating new fingerprint record for client with ID - " + patientId + " - [Finger position is " + fingerPosition + "]\n";
                    //System.out.println(fingerprint_processed_feedback);
                }
            } catch (Exception e) {
                System.out.println("[CIHP-0004] Error: " + e.getMessage());
            } finally {
//                fingerprint_processed_feedback = "Completed fingerprint processing for client with ID - " + patientId + " - [Finger position is " + fingerPosition + "]\n";
//                System.out.println(fingerprint_processed_feedback);
                try {
                    con.close();
                } catch (SQLException e) {
                    System.out.println("[CIHP-0005] Error closing DB connect => " + e.getMessage());
                }
            }
        } else {
            // Show Patients that are not found in the Database
            fingerprint_processed_feedback = "NOT FOUND ::: Client with uuid[" + patient_uuid + "]\n";
        }
    }

    private int GetPatientIDFromUUID(String patient_Uuid) {
        DbConnector jdbc = new DbConnector();
        Connection con = jdbc.dbDriver();
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
        } finally {
            try {
                con.close();
            } catch (SQLException e) {
                System.out.println("[CIHP-0003] Error closing DB connect => " + e.getMessage());
            }
        }
        return 0;
    }
}
