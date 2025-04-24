package com.example.pcimg;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageUtils {

    public static BufferedImage load(String path) throws IOException {
        return ImageIO.read(new File(path));
    }
    public static BufferedImage resizeImage(BufferedImage original, int width, int height) {
        BufferedImage resized_image = new BufferedImage(width, height, original.getType());
        Graphics2D graphics_for_image = resized_image.createGraphics();
        graphics_for_image.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics_for_image.drawImage(original, 0, 0, width, height, null);
        graphics_for_image.dispose();
        return resized_image;
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
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        double[][] data = matrix.getValues();
        double[] row = data[0];
        int pixels = w * h;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int idx = y * w + x;
                int r = (int)(row[idx] );
                int g = (int)(row[pixels + idx] );
                int b = (int)(row[2 * pixels + idx] );
                r = Math.min(255, Math.max(0, r));
                g = Math.min(255, Math.max(0, g));
                b = Math.min(255, Math.max(0, b));
                int rgb = (r << 16) | (g << 8) | b;
                img.setRGB(x, y, rgb);
            }
        }

        return img;
    }
}
