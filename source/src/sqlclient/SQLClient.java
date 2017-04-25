/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sqlclient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import sqlclient.view.MainController;

/**
 *
 * @author Morgan Xu
 */
public class SQLClient extends Application {
    
    private Stage mainStage;
    private Scene mainScene;
    private AnchorPane rootPane;
    
    @Override
    public void start(Stage primaryStage){
        this.mainStage = primaryStage;
        this.mainStage.setTitle("Embedded DB Workbench");
        this.mainStage.setResizable(true);
        initRootLayout();
        this.mainStage.show();
    }

    private void initRootLayout() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(SQLClient.class.getResource("view/Main.fxml"));
            rootPane = (AnchorPane) loader.load();
            mainScene = new Scene(rootPane);
            MainController mainController = loader.getController();
            mainController.setMainApp(this);
            // Set the scene containing the root layout.
            mainStage.setScene(mainScene);
            
        } catch (IOException ex) {
            Logger.getLogger(SQLClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public Stage getStage(){
        return mainStage;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
