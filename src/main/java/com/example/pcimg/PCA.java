package com.example.pcimg;

import java.io.*;
import java.util.Arrays;


public class PCA implements Serializable {
    private static final long serialVersionUID = 1L;
    public Matrix v;
    public Matrix avg;
    static long time = 0;

    /**
     * Normalizes the data matrix by subtracting the average of each feature.
     * <p>
     * The method computes the average (mean) for each feature across all samples,
     * stores the computed mean in {@code avg}, and subtracts it from each sample in {@code x}.
     * </p>
     *
     * @param x the data matrix where each row represents a sample and each column represents a feature
     */
    private void normalizeData(Matrix x) {
        int samples = x.getHeight();
        int features = x.getWidth();
        avg = Matrix.zeros(1, features);
        for (int r = 0; r < samples; r++) {
            avg = avg.add(x.getR(r));
        }
        avg = avg.dot(1.0f / samples);
        for (int i = 0; i < samples; i++) {
            for (int j = 0; j < features; j++) {
                x.set(i, j, x.get(i, j) - avg.get(0, j));
            }
        }
    }

    /**
     * Sorts the eigenvectors in descending order based on their corresponding eigenvalues.
     * <p>
     * The method extracts the eigenvalues from the diagonal of the provided matrix {@code values},
     * then sorts the eigenvector columns of {@code vectors} according to these eigenvalues.
     * </p>
     *
     * @param values  a diagonal matrix where each diagonal element is an eigenvalue
     * @param vectors a matrix whose columns are the corresponding eigenvectors
     * @return a new {@code Matrix} containing the eigenvectors sorted in descending order of eigenvalue magnitude
     */
    public static Matrix sortEigenVectors(Matrix values, Matrix vectors) {
        int size = values.getWidth();
        double[] eigenvalues = new double[size];

        for (int i = 0; i < size; i++) {
            eigenvalues[i] = values.getValues()[i][i];
        }

        Integer[] indexes = new Integer[size];
        for (int i = 0; i < size; i++) {
            indexes[i] = i;
        }
        Arrays.sort(indexes, (i1, i2) -> -Double.compare(eigenvalues[i1], eigenvalues[i2]));

        Matrix sortedVectors = Matrix.zeros(vectors.getHeight(), vectors.getWidth());
        for (int i = 0; i < size; i++) {
            sortedVectors.setC(vectors.getC(indexes[i]), i);
        }
        return sortedVectors;
    }

    /**
     * Constructs a new {@code PCA} object for data with the specified number of features.
     *
     * @param features the number of features (columns) in the input data
     */
    public PCA(int features) {
        v = null;
        avg = Matrix.zeros(1, features);
    }

    /**
     * Fits the PCA model to the provided data.
     * <p>
     * This method normalizes the data, computes the covariance matrix, performs eigen decomposition
     * on the covariance matrix, and sorts the eigenvectors by eigenvalue magnitude. Only the top
     * {@code outfeatures} eigenvectors are retained.
     * </p>
     *
     * @param x           the input data matrix where each row is a sample and each column is a feature
     * @param outfeatures the number of principal components (eigenvectors) to retain
     */
    public void fit(Matrix x, int outfeatures) throws Exception {
        x = x.clone();
        System.out.println("Normalizing");
        normalizeData(x);
        System.out.println("covving");
        Matrix cov = x.transpose().dot(x);

        // Optional: print a specific row from the covariance matrix
        System.out.println(cov.getR(391));



        System.out.println("Eigening");
        Matrix.Pair eigenPair = Matrix.eigen(cov);
        Matrix sortedVectors = sortEigenVectors(eigenPair.getFirst(), eigenPair.getSecond());
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("maniak.txt"))) {
            int rows = eigenPair.getSecond().getHeight();
            int cols = eigenPair.getSecond().getWidth();
            for (int i = 0; i < rows; i++) {
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < cols; j++) {
                    sb.append(eigenPair.getSecond().get(i, j));
                    if (j < cols - 1) {
                        sb.append(", ");
                    }
                }
                writer.write(sb.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing covariance matrix to file: " + e.getMessage());
        }
        v = sortedVectors.getSubMatrix(0, sortedVectors.getHeight(), 0, outfeatures);
    }

    /**
     * Retrieves the i-th eigenvector (principal component).
     *
     * @param i the index of the eigenvector to retrieve
     * @return a column vector (as a {@code Matrix}) representing the i-th eigenvector
     */
    public Matrix getEigenvector(int i) {
        return v.getC(i);
    }

    /**
     * Encodes the input data into a lower-dimensional representation using the fitted PCA model.
     * <p>
     * The method normalizes the data by subtracting the mean and then projects it onto the subspace
     * defined by the eigenvectors stored in {@code v}.
     * </p>
     *
     * @param data the original data matrix to encode
     * @return a new {@code Matrix} representing the data in the reduced-dimensional space
     */
    public Matrix encode(Matrix data) {
        Matrix x = data.clone();
        int samples = x.getHeight();
        int features = x.getWidth();
        // Subtract the computed average from each sample.
        for (int i = 0; i < samples; i++) {
            for (int j = 0; j < features; j++) {
                x.set(i, j, x.get(i, j) - avg.get(0, j));
            }
        }
        System.out.println(data.getHeight() + "-Height");
        System.out.println(data.getWidth() + "-Width");
        // Project the normalized data onto the principal components.
        return x.dot(v);
    }

    /**
     * Decodes the lower-dimensional representation back into the original data space.
     * <p>
     * The method projects the encoded data back to the original space using the transposed eigenvector matrix,
     * and then adds back the average that was subtracted during encoding.
     * </p>
     *
     * @param encoded the encoded data matrix
     * @return a new {@code Matrix} representing the reconstructed data in the original space
     */
    public Matrix decode(Matrix encoded) {
        Matrix x = encoded.dot(v.transpose());
        int samples = x.getHeight();
        int features = x.getWidth();
        // Add the average back to each sample.
        for (int i = 0; i < samples; i++) {
            for (int j = 0; j < features; j++) {
                x.set(i, j, x.get(i, j) + avg.get(0, j));
            }
        }
        return x;
    }

    /**
     * Decodes the lower-dimensional representation back into the original data space without adding the average.
     * <p>
     * This method is similar to {@link #decode(Matrix)} but skips adding the mean back into the data.
     * </p>
     *
     * @param encoded the encoded data matrix
     * @return a new {@code Matrix} representing the decoded data without the mean adjustment
     */
    public Matrix decode_noavg(Matrix encoded) {
        return encoded.dot(v.transpose());
    }

    /**
     * Saves the current {@code PCA} object to a file using serialization.
     *
     * @param filename the path to the file where the PCA model will be saved
     * @throws IOException if an I/O error occurs during writing to the file
     */
    public void saveToFile(String filename) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(this);
            System.out.println("PCA saved to file: " + filename);
        }
    }

    /**
     * Loads a {@code PCA} object from a file.
     *
     * @param filename the path to the file from which to load the PCA model
     * @return the {@code PCA} object loaded from the file
     * @throws IOException            if an I/O error occurs during reading from the file
     * @throws ClassNotFoundException if the class of the serialized object cannot be found
     */
    public static PCA loadFromFile(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            PCA pca = (PCA) ois.readObject();
            System.out.println("PCA loaded from file: " + filename);
            return pca;
        }
    }
}
