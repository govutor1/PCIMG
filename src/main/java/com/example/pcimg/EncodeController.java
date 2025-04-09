package com.example.pcimg;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class EncodeController {

    @FXML private Label titleLabel;

    @FXML private Label imageLabel;

    @FXML private TextField imageTextField;

    @FXML private Label fitLabel;

    @FXML private TextField fitTextField;

    @FXML private Button encodeButton;

    @FXML private Button greetingButton;

    /**
     * Opens a file chooser dialog to select an image file.
     * <p>
     * The method filters for common image file extensions and sets the selected file's absolute path
     * to {@code imageTextField}.
     * </p>
     *
     * @param evt the action event triggered by the user
     */
    @FXML
    private void onBrowseImage(ActionEvent evt) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Image File");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.bmp")
        );

        File file = chooser.showOpenDialog(getWindow(evt));
        if (file != null) {
            imageTextField.setText(file.getAbsolutePath());
        }
    }

    /**
     * Opens a file chooser dialog to select a PCA fit file.
     * <p>
     * The method filters for binary files (with extensions *.bin and *.dat) and sets the selected file's
     * absolute path to {@code fitTextField}.
     * </p>
     *
     * @param evt the action event triggered by the user
     */
    @FXML
    private void onBrowseFit(ActionEvent evt) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select PCA Fit File");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Binary Files", "*.bin", "*.dat")
        );

        File file = chooser.showOpenDialog(getWindow(evt));
        if (file != null) {
            fitTextField.setText(file.getAbsolutePath());
        }
    }

    /**
     * Retrieves the {@link Window} associated with the given {@link ActionEvent}.
     *
     * @param evt the action event from which to retrieve the window
     * @return the window associated with the event source
     */
    private Window getWindow(ActionEvent evt) {
        return ((Node)evt.getSource()).getScene().getWindow();
    }

    /**
     * Initializes the controller.
     * <p>
     * This method is automatically called after the FXML fields are injected.
     * It sets the action for the encode button to trigger the encoding process.
     * </p>
     */
    @FXML
    private void initialize() {
        encodeButton.setOnAction(e -> {
            try {
                onEncodeButtonClick();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    /**
     * Handles the action when the encode button is clicked.
     * <p>
     * The method reads the image file path and PCA fit file path from the text fields,
     * loads the PCA model, and reads the image file. It then converts the image to a row matrix,
     * encodes it using the PCA model, and saves the encoded matrix to a binary file.
     * Finally, it prints the dimensions of the encoded matrix.
     * </p>
     *
     * @throws IOException if an I/O error occurs during file operations
     * @throws ClassNotFoundException if the PCA class cannot be found during deserialization
     */
    @FXML
    private void onEncodeButtonClick() throws IOException, ClassNotFoundException {
        String imagePath = imageTextField.getText();
        String pcaPath   = fitTextField.getText();

        PCA loadedPCA = PCA.loadFromFile(pcaPath);
        BufferedImage img = ImageUtils.loadImage(imagePath);

        Matrix imgRow = ImageUtils.imageToRGBRowMatrix(img);
        Matrix encoded = loadedPCA.encode(imgRow);
        System.out.println(loadedPCA.getEigenvector(0).norm());
        encoded.saveToFile("encoded_matrix.bin");

        System.out.println("Encoded shape: " + encoded.getHeight() + "×" + encoded.getWidth());
    }

    /**
     * Navigates back to the greeting screen.
     * <p>
     * This method loads the "GreetingScreen.fxml" file, sets it as the current scene, and displays the greeting screen.
     * </p>
     *
     * @param event the action event triggered by the user
     * @throws IOException if the FXML file cannot be loaded
     */
    @FXML
    private void handleGreetingScreen(ActionEvent event) throws IOException {
        Parent greetingRoot = FXMLLoader.load(getClass().getResource("GreetingScreen.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(greetingRoot));
        stage.show();
    }
}
