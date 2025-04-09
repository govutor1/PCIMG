package com.example.pcimg;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import java.io.IOException;


public class GreetingScreenController {

    /**
     * Handles the event when the "Encode" button is clicked.
     * <p>
     * This method loads the "EncodeScreen.fxml" page and sets it as the current scene.
     * </p>
     *
     * @param event the action event triggered by clicking the Encode button
     */
    @FXML
    private void handleEncode(ActionEvent event) {
        loadPage(event, "EncodeScreen.fxml");
    }

    /**
     * Handles the event when the "Decode" button is clicked.
     * <p>
     * This method loads the "DecodeScreen.fxml" page and sets it as the current scene.
     * </p>
     *
     * @param event the action event triggered by clicking the Decode button
     */
    @FXML
    private void handleDecode(ActionEvent event) {
        loadPage(event, "DecodeScreen.fxml");
    }

    /**
     * Handles the event when the "Fit" button is clicked.
     * <p>
     * This method loads the "FitScreen.fxml" page and sets it as the current scene.
     * </p>
     *
     * @param event the action event triggered by clicking the Fit button
     */
    @FXML
    private void handleFit(ActionEvent event) {
        loadPage(event, "FitScreen.fxml");
    }

    /**
     * Loads the specified FXML page and sets it as the current scene.
     * <p>
     * This helper method is used to change the current scene based on user actions.
     * </p>
     *
     * @param event    the action event that triggered the page load
     * @param fxmlFile the name of the FXML file to load
     */
    private void loadPage(ActionEvent event, String fxmlFile) {
        try {
            Parent page = FXMLLoader.load(getClass().getResource(fxmlFile));
            Scene scene = new Scene(page);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
