package com.example.pcimg;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class FitController {
    private pcadb db;

    public TextField imageName;
    @FXML
    private Label titleLabel;

    @FXML
    private Label datasetLabel;
    @FXML
    private TextField datasetTextField;
    @FXML
    private Button fitButton;


    @FXML
    private void initialize() {
        try {
            String url  = "jdbc:postgresql://localhost:5432/yippe";
            String user = "postgres";
            String pass = "postgres";
            Connection con = DriverManager.getConnection(url, user, pass);
            db = new pcadb(con);
            db.createTable();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to initialize PCA spinner", ex);
        }
    }

    /**
     * Handles the action to navigate back to the greeting screen.
     * <p>
     * This method loads the "GreetingScreen.fxml" file, sets it as the current scene,
     * and displays the greeting screen.
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
     * Loads a CSV file from the given file path and converts it into a 2D array of doubles.
     * <p>
     * The method can optionally skip the header row and the first column.
     * </p>
     *
     * @param filePath         the path to the CSV file
     * @param skipHeader       {@code true} to skip the header row; {@code false} otherwise
     * @param skipFirstColumn  {@code true} to skip the first column; {@code false} otherwise
     * @return a two-dimensional array of doubles representing the CSV data
     * @throws IOException if an I/O error occurs during file reading
     */
    public static double[][] loadCsv(String filePath, boolean skipHeader, boolean skipFirstColumn) throws IOException {
        int linenum=0;
        List<double[]> rows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            if (skipHeader) {
                br.readLine();
            }
            String line;
            while (((line = br.readLine()) != null)&&(linenum!=35000)) {
                String[] tokens = line.split(",");
                int startIndex = skipFirstColumn ? 1 : 0;
                double[] row = new double[tokens.length - startIndex];
                for (int i = startIndex; i < tokens.length; i++) {
                    row[i - startIndex] = Double.parseDouble(tokens[i].trim());
                }
                rows.add(row);
                linenum++;
            }
        }
        double[][] data = new double[rows.size()][];
        for (int i = 0; i < rows.size(); i++) {
            data[i] = rows.get(i);
        }
        return data;
    }

    /**
     * Reshapes a one-dimensional array into a two-dimensional array with the specified number of rows and columns.
     *
     * @param data the one-dimensional array of doubles
     * @param rows the number of rows for the reshaped array
     * @param cols the number of columns for the reshaped array
     * @return a two-dimensional array of doubles with the specified dimensions
     */
    public static double[][] reshapeTo2D(double[] data, int rows, int cols) {
        double[][] reshaped = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(data, i * cols, reshaped[i], 0, cols);
        }
        return reshaped;
    }

    /**
     * Handles the event when the "Fit" button is clicked.
     * <p>
     * This method reads the dataset file path from the text field, loads the CSV data,
     * constructs a {@link Matrix} from the data, displays a sample image if possible, fits a PCA model,
     * and saves the fitted PCA model to a file.
     * </p>
     *
     * @throws IOException if an error occurs during file I/O operations
     */
    @FXML
    private void onFitButtonClick() throws IOException, SQLException {
        String filePath = datasetTextField.getText();
        String name=imageName.getText();
        boolean skipHeader = true;
        boolean skipFirstColumn = false;
        double[][] csvData = loadCsv(filePath, skipHeader, skipFirstColumn);
        Matrix dataMatrix = new Matrix(csvData);
        System.out.println("Dataset dimensions: " + dataMatrix.getHeight() + " rows and " + dataMatrix.getWidth() + " columns");
        int width = (int) Math.sqrt(dataMatrix.getColumnCount()/3);
        int height = (int) Math.sqrt(dataMatrix.getColumnCount());
        double[][] firstSample = new double[1][csvData[0].length];
        firstSample[0]=csvData[9];
        Matrix firstsamplemat=new Matrix(firstSample);
        System.out.println(firstsamplemat);
        System.out.println("YPPIEW");
            BufferedImage image =  ImageUtils.rowMatrixToImage(firstsamplemat, width, width);
            Matrix imagemat=ImageUtils.imageToRGBRowMatrix(image);
            image=ImageUtils.rowMatrixToImage(imagemat,width,width);
            CSVLoader.displayImage(image, "First Sample");
        System.out.println(imagemat);
        PCA pca = new PCA(dataMatrix.getWidth());
        pca.fit(dataMatrix, dataMatrix.getColumnCount() *6/10);
        db.savePCA(name,pca);
        for (int i = 0; i <10 ; i++) {
            System.out.println(i);
            System.out.println(pca.v.getC(i));

        }
        System.out.println("AVG:");
        System.out.println(pca.avg);
    }

    /**
     * Opens a file chooser dialog to browse and select a dataset file.
     * <p>
     * The selected file's path is then displayed on the {@code datasetLabel}.
     * </p>
     *
     * @param evt the action event triggered by the user
     */
    @FXML
    private void onBrowseDataset(ActionEvent evt) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select PCA Fit File");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Binary Files", "*.bin", "*.dat")
        );

        File file = chooser.showOpenDialog(getWindow(evt));
        if (file != null) {
            datasetLabel.setText(file.getAbsolutePath());
        }
    }

    /**
     * Retrieves the {@link Window} from an {@link ActionEvent}.
     *
     * @param evt the action event from which to retrieve the window
     * @return the window associated with the event source
     */
    private Window getWindow(ActionEvent evt) {
        return ((Node) evt.getSource()).getScene().getWindow();
    }
}
