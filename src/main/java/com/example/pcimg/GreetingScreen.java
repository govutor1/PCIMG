package com.example.pcimg;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.concurrent.ExecutionException;

public class GreetingScreen extends Application {

    /**
     * The main entry point for all JavaFX applications.
     *
     * @param primaryStage the primary stage for this application, onto which the application scene can be set
     * @throws Exception if the FXML resource cannot be loaded
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("GreetingScreen.fxml"));
        primaryStage.setTitle("Greeting Screen");
        primaryStage.setScene(new Scene(root, 400, 300));
        primaryStage.show();
    }

    /**
     * The main method which launches the JavaFX application.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        double[][] arr = {{1,2,3},{2,4,5},{3,5,6}};
       Matrix mat=new Matrix(arr);
        System.out.println(Matrix.eigen(mat).getSecond());





        launch(args);
    }
}
