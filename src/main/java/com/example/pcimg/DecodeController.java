package com.example.pcimg;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;


public class DecodeController {
    private  pcadb db;
    @FXML private Label titleLabel;
    @FXML private TextField compressedImageTextField;
    @FXML private TextField fitTextField;
    @FXML private Button decodeButton;
    @FXML private Spinner<String> pcaSpinner;

    /**
     * Initializes the controller.
     * <p>
     * This method sets the action for the decode button to call {@code onDecodeButtonClick()} when triggered.
     * </p>
     */
    @FXML
    private void initialize() {
        try {
            String url  = "jdbc:postgresql://localhost:5432/yippe";
            String user = "postgres";
            String pass = "postgres";
            Connection con = DriverManager.getConnection(url, user, pass);
            db = new pcadb(con);
            db.createTable();
            List<String> names = db.list();
            ObservableList<String> items = FXCollections.observableArrayList(names);
            SpinnerValueFactory<String> factory =
                    new SpinnerValueFactory.ListSpinnerValueFactory<>(items);
            pcaSpinner.setValueFactory(factory);

        } catch (Exception ex) {
            throw new RuntimeException("Failed to initialize PCA spinner", ex);
        }
        decodeButton.setOnAction(evt -> {
            try {
                onDecodeButtonClick();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Handles the decoding process when the decode button is clicked.
     * <p>
     * The method reads the compressed matrix file path and PCA fit file path from the text fields,
     * loads the PCA model and compressed matrix, decodes the matrix using the PCA model,
     * converts the result to a scaled image, and saves the image as "decoded_output.png".
     * </p>
     *
     * @throws IOException if an I/O error occurs during file operations
     * @throws ClassNotFoundException if the PCA class cannot be found during deserialization
     */
    @FXML
    private void onDecodeButtonClick() throws IOException, ClassNotFoundException, SQLException {
        String matrixPath = compressedImageTextField.getText();
        String model=pcaSpinner.getValue();
        PCA loadedPCA = db.loadPCA(model);
        Matrix mat= Matrix.loadFromFile(matrixPath);
        Matrix imat= loadedPCA.decode(mat);
        BufferedImage decoded_img = ImageUtils.rowMatrixToImage(imat, (int) Math.sqrt(imat.getWidth()*imat.getHeight()/3),(int) Math.sqrt(imat.getWidth()*imat.getHeight()/3));
        File out = new File("decoded_output.png");

        ImageIO.write(decoded_img, "png", out);
        System.out.println("Decoded image saved to: " + out.getAbsolutePath());
        System.out.println("encoded");
        System.out.println(mat);
        System.out.println("mefuanah");
        System.out.println(imat);
    }

    /**
     * Opens a file chooser to browse and select a compressed matrix file.
     * <p>
     * The method filters for binary files with extensions ".bin" or ".dat" and sets the selected file's
     * absolute path into the {@code compressedImageTextField}.
     * </p>
     *
     * @param evt the action event triggered by the user
     */
    @FXML
    private void Browse(ActionEvent evt) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Compressed Matrix File");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Binary Files", "*.bin", "*.dat")
        );
        File file = chooser.showOpenDialog(getWindow(evt));
        if (file != null) {
            compressedImageTextField.setText(file.getAbsolutePath());
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
     * Converts a {@link Matrix} to a scaled {@link BufferedImage}.
     * <p>
     * The method determines whether the matrix represents a color image (if the number of columns is a multiple of 3)
     * or a grayscale image, reconstructs the image accordingly, and scales it by the given scale factor.
     * </p>
     *
     * @param mat the {@code Matrix} to convert into an image
     * @param scaleFactor the factor by which to scale the resulting image
     * @return a {@link BufferedImage} representing the scaled image
     */
    private BufferedImage matrixToScaledImage(Matrix mat, int scaleFactor) {
        int rawWidth = mat.getWidth();
        int height   = mat.getHeight();
        boolean isColor = (rawWidth % 3 == 0);
        int width  = isColor ? rawWidth / 3 : rawWidth;
        int imageType = isColor
                ? BufferedImage.TYPE_INT_RGB
                : BufferedImage.TYPE_BYTE_GRAY;
        BufferedImage img = new BufferedImage(width, height, imageType);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (isColor) {
                    int r = to8Bit(mat.get(y, x));
                    int g = to8Bit(mat.get(y, x + width));
                    int b = to8Bit(mat.get(y, x + 2 * width));
                    int rgb = (r << 16) | (g << 8) | b;
                    img.setRGB(x, y, rgb);
                } else {
                    int gray = to8Bit(mat.get(y, x));
                    int rgb = (gray << 16) | (gray << 8) | gray;
                    img.setRGB(x, y, rgb);
                }
            }
        }
        if (scaleFactor != 1) {
            int sw = width * scaleFactor;
            int sh = height * scaleFactor;
            Image tmp = img.getScaledInstance(sw, sh, Image.SCALE_SMOOTH);
            return toBufferedImage(tmp);
        }
        return img;
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

    /**
     * Converts a raw double value to an 8-bit integer.
     * <p>
     * The method normalizes the input value to the range [0, 1] (if necessary) and scales it to the
     * 0-255 range.
     * </p>
     *
     * @param raw the raw double value to convert
     * @return an integer in the range 0 to 255 representing the 8-bit value
     */
    private int to8Bit(double raw) {
        double norm = raw <= 1.0 ? raw : (raw / 255.0);
        norm = Math.max(0.0, Math.min(1.0, norm));
        return (int)Math.round(norm * 255);
    }

    /**
     * Converts an {@link Image} object into a {@link BufferedImage}.
     * <p>
     * If the input image is already a {@code BufferedImage}, it is returned directly.
     * Otherwise, a new {@code BufferedImage} is created and the input image is drawn onto it.
     * </p>
     *
     * @param img the image to convert
     * @return a {@link BufferedImage} representation of the input image
     */
    public static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) return (BufferedImage)img;
        BufferedImage bimg = new BufferedImage(
                img.getWidth(null),
                img.getHeight(null),
                BufferedImage.TYPE_INT_ARGB
        );
        Graphics2D graphics = bimg.createGraphics();
        graphics.drawImage(img, 0, 0, null);
        graphics.dispose();
        return bimg;
    }
}
