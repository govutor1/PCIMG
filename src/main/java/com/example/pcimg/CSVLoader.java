package com.example.pcimg;


import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CSVLoader {

    public static double[][] loadCsv(String filePath, boolean skipHeader) throws IOException {
        List<double[]> rows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            if (skipHeader) {
                br.readLine();
            }
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(",");
                double[] row = new double[tokens.length];
                for (int i = 0; i < tokens.length; i++) {
                    row[i] = Double.parseDouble(tokens[i].trim());
                }
                rows.add(row);
            }
        }
        double[][] data = new double[rows.size()][];
        for (int i = 0; i < rows.size(); i++) {
            data[i] = rows.get(i);
        }
        return data;
    }

    public static BufferedImage matrixToScaledImage(Matrix matrix, int scaleFactor) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int gray = (int) (matrix.get(y, x) * 255);
                gray = Math.max(0, Math.min(255, gray));
                int rgb = new Color(gray, gray, gray).getRGB();
                image.setRGB(x, y, rgb);
            }
        }

        int scaledWidth = width * scaleFactor;
        int scaledHeight = height * scaleFactor;
        Image scaled = image.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
        return toBufferedImage(scaled);
    }

    public static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bimage.createGraphics();
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();
        return bimage;
    }

    public static void displayImage(BufferedImage image, String title) {
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new JLabel(new ImageIcon(image)));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static double[][] reshapeTo2D(double[] array, int rows, int cols) {
        if (array.length != rows * cols) {
            throw new IllegalArgumentException("Array length does not match specified dimensions");
        }
        double[][] reshaped = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(array, i * cols, reshaped[i], 0, cols);
        }
        return reshaped;
    }

    public static void main(String[] args) {
        try {
            String filePath = "C:\\Users\\roeia\\IdeaProjects\\PCAMagniv\\src\\META-INF\\your_dataset.csv";
            double[][] data = loadCsv(filePath, true);
            System.out.println("Dataset dimensions: " + data.length + " rows and " + (data.length > 0 ? data[0].length : 0) + " columns.");

            if (data.length > 0) {
                int numColumns = data[0].length;
                int imageSize = (int) Math.sqrt(numColumns);
                if (imageSize * imageSize == numColumns) {
                    boolean hasLabel = false;
                    double[] pixels;
                    String labelText = "";
                    if (hasLabel) {
                        int label = (int) data[0][0];
                        labelText = "Label: " + label;
                        pixels = new double[numColumns - 1];
                        System.arraycopy(data[0], 1, pixels, 0, numColumns - 1);
                    } else {
                        pixels = data[0];
                    }
                    double[][] pixelMatrix = reshapeTo2D(pixels, imageSize, imageSize);
                    Matrix imageMatrix = new Matrix(pixelMatrix);
                    int scaleFactor = 10;
                    BufferedImage image = matrixToScaledImage(imageMatrix, scaleFactor);
                    displayImage(image, "Image " + labelText);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading CSV file: " + e.getMessage());
        }
    }
}
