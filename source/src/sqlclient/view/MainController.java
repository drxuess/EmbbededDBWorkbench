/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sqlclient.view;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;
import sqlclient.SQLClient;
import sqlclient.database.DatabaseConnection;
import sqlclient.model.SQLCodeArea;
import sqlclient.model.SQLObject;

/**
 * FXML Controller class
 *
 * @author Morgan
 */
public class MainController implements Initializable {

    private SQLClient mainApp;
    private Stage mainStage;
    private File selectedDir = new File(System.getProperty("user.dir"));
    private DatabaseConnection dbc;

    //FXML variables
    @FXML
    private TextField dbPathTextField;
    @FXML
    private Button btnBrowse;
    @FXML
    private Button btnConnect;
    
    @FXML
    private RadioButton rbDerby;
    @FXML
    private RadioButton rbH2;
    //@FXML
    //private TextArea sqlArea;
    @FXML
    private TableView<ArrayList<String>> resultsTable;
    @FXML
    private TextArea logArea;
    @FXML
    private Button runButton;
    @FXML
    private StackPane codeContainerPane;

    //Custom Code Pane
    CodeArea codeArea;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dbPathTextField.setText(selectedDir.toString());
        //Extra Code
        logArea.setEditable(false);
        codeArea = new SQLCodeArea().getCodeArea();
        codeArea.setDisable(true);
        codeContainerPane.getChildren().add(codeArea);
    }

    //Integration Methods
    public void setMainApp(SQLClient main) {
        this.mainApp = main;
        this.mainStage = main.getStage();
    }

    //FXML Button Methods
    @FXML
    public void handleBrowseButton() {
        if(rbDerby.isSelected()){
            DirectoryChooser dirChooser = new DirectoryChooser();
            dirChooser.setTitle("Choose directory of your Derby database");
            dirChooser.setInitialDirectory(selectedDir);
            selectedDir = dirChooser.showDialog(mainStage);
            dbPathTextField.setText(selectedDir.toString());
        }
        else if(rbH2.isSelected()){
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose file of your H2 database");
            fileChooser.setInitialDirectory(selectedDir);
            fileChooser.setSelectedExtensionFilter(new ExtensionFilter(".db Files", ".db"));
            selectedDir = fileChooser.showOpenDialog(mainStage);
            dbPathTextField.setText(selectedDir.toString());
        }
    }

    @FXML
    public void handleConnectButton() {
        if (rbDerby.isSelected()) {
            try {
                if (Files.exists(Paths.get(dbPathTextField.getText()))) {
                    if (Files.exists(Paths.get(dbPathTextField.getText() + File.separator + "service.properties"))) {
                        connect();
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error Folder is not a DB");
                        alert.setContentText("Folder is not a DB folder."
                                + " Please specify a DB folder or a non-existent directory");
                        alert.showAndWait();
                    }
                } else {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirmation Dialog");
                    alert.setHeaderText("Creating Database");
                    alert.setContentText("Are you sure you want to create a database here?");

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.get() == ButtonType.OK) {
                        connect();
                    } else {
                        alert.close();
                    }
                }
            } catch (SQLException ex) {
                exceptionAlert(ex);
                Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if(rbH2.isSelected()){
            try{
                if (Files.exists(Paths.get(dbPathTextField.getText()))) {
                    connect();
                }
                else {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirmation Dialog");
                    alert.setHeaderText("No Database exists here.");
                    alert.setContentText("Do you want to create a H2 database here?");

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.get() == ButtonType.OK) {
                        connect();
                    } else {
                        alert.close();
                    }
                }
            } catch (SQLException ex){
                exceptionAlert(ex);
                Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    @FXML
    public void handleRunButton() {

        String sql = codeArea.getText();
        List<String> queries = new ArrayList<String>();
        for (String query : sql.split(";")) {
            queries.add(query);
        }
        executeQueries(queries);

    }

    //SQL Methods
    private void executeQueries(List<String> queries) {
        for (String query : queries) {
            try {
                query = query.trim();
                System.out.println(query);
                SQLObject sqlObject = dbc.execute(query);
                displayTable(sqlObject);
            } catch (SQLException ex) {
                exceptionAlert(ex);
                Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
                break;
            }
        }
    }

    //Utility Methods
    public void log(String message) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        logArea.setText(logArea.getText() + "\n" + sdf.format(new Timestamp(new Date().getTime())) + "  " + message);
    }

    @FXML
    private void clearLog() {
        logArea.clear();
    }

    //Connect to Database
    private void connect() throws SQLException {
        if (rbDerby.isSelected()) {
            dbc = new DatabaseConnection(dbPathTextField.getText());
            dbc.setDriver("derby");
        } else if (rbH2.isSelected()) {
            String path = dbPathTextField.getText();
            dbc = new DatabaseConnection(path.substring(0, path.length()-6));
            dbc.setDriver("h2");
        }
        dbc.openConnection();
        dbc.closeConnection();
        displayTable(dbc.listAllTables());
        runButton.disableProperty().set(false);
        codeArea.setDisable(false);
        dbPathTextField.setDisable(true);
        btnBrowse.setDisable(true);
        btnConnect.setDisable(true);
        log("Connected to Database");
    }

    //Display in Table
    private void displayTable(SQLObject sqlObject) {
        resultsTable.getColumns().clear();
        List<String> columns = sqlObject.getColumnHeads();
        List<ArrayList<String>> results = sqlObject.getResults();
        String logMsg = sqlObject.getLogMsg();
        if (results != null) {
            ObservableList<ArrayList<String>> data = FXCollections.observableArrayList(results);
            resultsTable.setItems(data);
            for (String colName : columns) {
                TableColumn<ArrayList<String>, String> column = new TableColumn(colName);

                //column.setEditable(true);
                //column.setCellFactory(TextFieldTableCell.<ArrayList<String>>forTableColumn());
                //column.setOnEditCommit(new EventHandler);
                column.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().get(columns.indexOf(colName))));
                resultsTable.getColumns().add(column);
            }
            log(logMsg);
        } else {
            log(logMsg);
        }
    }

    //Print Exceptions
    private void exceptionAlert(Exception ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("SQL Syntax Error");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String exceptionText = sw.toString();

        GridPane expContent = new GridPane();

        Label label = new Label("The exception stacktrace was:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        alert.getDialogPane().setExpandableContent(expContent);

        alert.showAndWait();
    }
}
