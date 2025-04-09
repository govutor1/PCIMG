package com.example.pcimg;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageUtils {

    public static BufferedImage loadImage(String path) throws IOException {
        return ImageIO.read(new File(path));
    }

    public static Matrix imageToGrayscaleRowMatrix(BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight();
        double[][] data = new double[1][w * h];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = (rgb) & 0xFF;
                // convert to luminance (grayscale)
                double gray = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0;
                data[0][y * w + x] = gray;
            }
        }

        return new Matrix(data);
    }

    public static Matrix imageToRGBRowMatrix(BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight();
        double[][] data = new double[1][3 * w * h];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = (rgb) & 0xFF;
                int idx = y * w + x;
                data[0][idx] = r ;
                data[0][w * h + idx] = g ;
                data[0][2 * w * h + idx] = b ;
            }
        }

        return new Matrix(data);
    }


    /**
     * Converts a row vector (Matrix) back to a BufferedImage.
     * The input Matrix is assumed to be of size 1 x (3*w*h) where the first w*h elements are
     * the red channel, the next w*h are the green channel, and the last w*h are the blue channel.
     *
     * @param matrix the row vector representing the image.
     * @param w      the width of the image.
     * @param h      the height of the image.
     * @return       the reconstructed BufferedImage.
     */
    public static BufferedImage rowMatrixToImage(Matrix matrix, int w, int h) {
        // Create a new BufferedImage with RGB color model.
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        double[][] data = matrix.getValues();

        // Since the matrix is a single row, we access the first row.
        double[] row = data[0];
        int pixelCount = w * h;

        // Loop over each pixel coordinate.
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int idx = y * w + x;

                // For each channel, convert from normalized value [0,1] to integer in [0,255].
                int r = (int)(row[idx] );
                int g = (int)(row[pixelCount + idx] );
                int b = (int)(row[2 * pixelCount + idx] );

                // Clamp the values to ensure they are within 0 to 255.
                r = Math.min(255, Math.max(0, r));
                g = Math.min(255, Math.max(0, g));
                b = Math.min(255, Math.max(0, b));

                // Combine the color channels back into a single integer.
                int rgb = (r << 16) | (g << 8) | b;
                img.setRGB(x, y, rgb);
            }
        }

        return img;
    }
}
