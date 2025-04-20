package com.example.pcimg;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class Matrix implements Serializable {
    private static final long serialVersionUID = 1L;
    private double[][] values;
    private double[][] transposed;
    public static long time = 0;

    /**
     * Clones a two-dimensional array.
     *
     * @param array the array to clone
     * @return a deep copy of the provided two-dimensional array; or {@code null} if the array is null
     */
    public static double[][] clone2darray(double[][] array) {
        if (array == null) {
            return null;
        }
        double[][] copy = new double[array.length][];
        for (int i = 0; i < array.length; i++) {
            copy[i] = new double[array[i].length];
            System.arraycopy(array[i], 0, copy[i], 0, array[i].length);
        }
        return copy;
    }

    /**
     * Sets the matrix values using a two-dimensional array.
     *
     * @param values the two-dimensional array to set as the matrix values
     */
    public void setValues(double[][] values) {
        this.values = clone2darray(values);
    }

    /**
     * Retrieves the element at the specified row and column.
     *
     * @param y the row index
     * @param x the column index
     * @return the element at position (y, x)
     */
    public double get(int y, int x) {
        return values[y][x];
    }

    /**
     * Returns a string representation of the matrix.
     * Each row is printed on a new line with elements separated by commas.
     *
     * @return a string representing the matrix
     */
    public String toString() {
        String s = "";
        for (int i = 0; i < getHeight(); i++) {
            for (int j = 0; j < getWidth(); j++) {
                s += this.values[i][j];
                s += ", ";
            }
            s += "\n";
        }
        return s;
    }

    /**
     * Returns the specified row as a new {@code Matrix}.
     *
     * @param i the row index to retrieve
     * @return a matrix representing the i-th row
     */
    public Matrix getR(int i) {
        double[] arr = values[i];
        double[][] newarr = new double[1][arr.length];
        newarr[0] = arr;
        Matrix newm = new Matrix(newarr);
        return newm;
    }

    /**
     * Returns the specified column as a new {@code Matrix}.
     * This is achieved by transposing the matrix and retrieving the corresponding row.
     *
     * @param i the column index to retrieve
     * @return a matrix representing the i-th column
     */
    public Matrix getC(int i) {
        Matrix newm = transpose(this);
        return newm.getR(i);
    }

    /**
     * Sets the specified row of the matrix.
     *
     * @param row the matrix representing the new row (should be a 1 x n matrix)
     * @param i   the row index to set
     */
    public void setR(Matrix row, int i) {
        for (int j = 0; j < getWidth(); j++) {
            values[i][j] = row.values[0][j];
            transposed[j][i] = values[i][j];
        }
    }

    /**
     * Sets the specified column of the matrix.
     *
     * @param cull the matrix representing the new column (should be a 1 x m matrix)
     * @param i    the column index to set
     */
    public void setC(Matrix cull, int i) {
        for (int j = 0; j < getHeight(); j++) {
            values[j][i] = cull.values[0][j];
            transposed[i][j] = values[j][i];
        }
    }

    /**
     * Returns a deep copy of the matrix values.
     *
     * @return a two-dimensional array containing the matrix values
     */
    public double[][] getValues() {
        return clone2darray(values);
    }

    /**
     * Returns the number of columns in the matrix.
     *
     * @return the width of the matrix
     */
    public int getWidth() {
        return this.values[0].length;
    }

    /**
     * Returns the number of rows in the matrix.
     *
     * @return the height of the matrix
     */
    public int getHeight() {
        return this.values.length;
    }

    /**
     * Constructs a new {@code Matrix} using the given two-dimensional array.
     *
     * @param mat the two-dimensional array of values
     */
    public Matrix(double[][] mat) {
        this.values = clone2darray(mat);
        this.transposed = transpose2DArray(values);
    }

    /**
     * Constructs a new {@code Matrix} that is a deep copy of another {@code Matrix}.
     *
     * @param m the matrix to copy
     */
    public Matrix(Matrix m) {
        this.values = clone2darray(m.values);
        this.transposed = clone2darray(m.transposed);
    }

    /**
     * Returns a submatrix of the current matrix.
     *
     * @param i         the starting row index
     * @param height    the number of rows in the submatrix
     * @param i1        the starting column index
     * @param outfeature the number of columns in the submatrix
     * @return a new {@code Matrix} that is the specified submatrix
     */
    public Matrix getSubMatrix(int i, int height, int i1, int outfeature) {
        double[][] sub = new double[height][outfeature];
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < outfeature; col++) {
                sub[row][col] = this.values[i + row][i1 + col];
            }
        }
        return new Matrix(sub);
    }

    public double[][] values() {return values;
    }

    /**
     * A simple pair class to hold two matrices.
     * It is primarily used to return two matrices from operations like QR decomposition or eigenvalue computations.
     */
    public static class Pair {
        private Matrix first;
        private Matrix second;

        /**
         * Returns the first matrix in the pair.
         *
         * @return the first matrix
         */
        public Matrix getFirst() {
            return first;
        }

        /**
         * Returns the second matrix in the pair.
         *
         * @return the second matrix
         */
        public Matrix getSecond() {
            return second;
        }

        /**
         * Sets the first matrix in the pair.
         *
         * @param first the matrix to set as first
         */
        public void setFirst(Matrix first) {
            this.first = first;
        }

        /**
         * Sets the second matrix in the pair.
         *
         * @param second the matrix to set as second
         */
        public void setSecond(Matrix second) {
            this.second = second;
        }

        /**
         * Constructs a new {@code Pair} with the given matrices.
         *
         * @param m1 the first matrix
         * @param m2 the second matrix
         */
        public Pair(Matrix m1, Matrix m2) {
            first = m1;
            second = m2;
        }
    }

    /**
     * Computes the QR decomposition of a matrix using the Gram-Schmidt process.
     * Returns a {@link Pair} where the first element is the Q matrix and the second element is the R matrix.
     *
     * @param A the matrix to decompose
     * @return a {@code Pair} containing the Q and R matrices
     */
    public static Pair qr(Matrix A) {
        A = A.clone();
        int m = A.getHeight();
        int n = A.getWidth();

        double[][] Q = new double[m][n];
        double[][] R = new double[n][n];

        double[][] Avals = A.getValues();
        for (int j = 0; j < n; j++) {
            double[] v = new double[m];
            // Copy the jth column of A into v
            for (int i = 0; i < m; i++) {
                v[i] = Avals[i][j];
            }
            // Subtract the projection on all previous columns of Q
            for (int i = 0; i < j; i++) {
                double dot = 0;
                for (int k = 0; k < m; k++) {
                    dot += Q[k][i] * Avals[k][j];
                }
                R[i][j] = dot;
                for (int k = 0; k < m; k++) {
                    v[k] -= dot * Q[k][i];
                }
            }
            // Compute the norm of v
            double norm = 0;
            for (int i = 0; i < m; i++) {
                norm += v[i] * v[i];
            }
            norm = Math.sqrt(norm);

            // If the vector is nearly zero, complete the basis arbitrarily
            if (norm < 1e-12) {
                v = completeBasis(Q, j, m);
                norm = 0;
                for (int i = 0; i < m; i++) {
                    norm += v[i] * v[i];
                }
                norm = Math.sqrt(norm);
                R[j][j] = 0;  // since the original column was linearly dependent
            } else {
                R[j][j] = norm;
            }
            // Normalize and set as the jth column of Q
            for (int i = 0; i < m; i++) {
                Q[i][j] = v[i] / norm;
            }
        }
        Matrix Qmat = new Matrix(Q);
        Matrix Rmat = new Matrix(R);
        return new Pair(Qmat, Rmat);
    }

    /**
     * Completes the basis for the Gram-Schmidt process by finding a vector that is
     * orthogonal to the existing columns in Q.
     *
     * @param Q the matrix with already computed orthogonal vectors
     * @param j the current index for which the basis is incomplete
     * @param m the dimension of the vectors
     * @return an orthonormal vector that can complete the basis
     */
    private static double[] completeBasis(double[][] Q, int j, int m) {
        // Try each of the standard basis vectors
        for (int candidateIndex = 0; candidateIndex < m; candidateIndex++) {
            double[] candidate = new double[m];
            candidate[candidateIndex] = 1.0;
            // Orthogonalize candidate against the already computed columns of Q
            for (int col = 0; col < j; col++) {
                double dot = 0;
                for (int i = 0; i < m; i++) {
                    dot += candidate[i] * Q[i][col];
                }
                for (int i = 0; i < m; i++) {
                    candidate[i] -= dot * Q[i][col];
                }
            }
            // Compute the norm of the candidate vector
            double normCandidate = 0;
            for (int i = 0; i < m; i++) {
                normCandidate += candidate[i] * candidate[i];
            }
            normCandidate = Math.sqrt(normCandidate);
            // If candidate is not nearly zero, normalize and return it
            if (normCandidate != 0) {
                for (int i = 0; i < m; i++) {
                    candidate[i] /= normCandidate;
                }
                return candidate;
            }
        }
        // Fallback: this should not occur if j < m.
        return new double[m];
    }

    /**
     * Creates an identity matrix of size n x n.
     *
     * @param n the size of the identity matrix
     * @return an identity {@code Matrix} of size n x n
     */
    public static Matrix eye(int n) {
        Matrix m = Matrix.zeros(n, n);
        for (int i = 0; i < n; i++) {
            m.values[i][i] = 1;
            m.transposed[i][i] = 1;
        }
        return m;
    }

    /**
     * Returns the Hadamard product of the matrix with an identity matrix of the same size.
     *
     * @param m the matrix to combine with an identity matrix
     * @return a new {@code Matrix} representing the Hadamard product
     */
    public static Matrix diag(Matrix m) {
        return m.hadamard(Matrix.eye(m.getHeight()));
    }


    public static Pair eigen(Matrix A) throws Exception {

        return EigenCalculator.calculateEigen(A);
    }



    /**
     * Generates rounds (disjoint sets of pivot pairs) via round-robin (1-factorization).
     * For an even number n, there are n-1 rounds; for odd n, we add one dummy index.
     */

    /**
     * Computes the Frobenius norm of the off-diagonal elements.
     */
    public static double offDiagonalNorm(double[][] A) {
        int n = A.length;
        double sum = 0.0;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                sum += A[i][j] * A[i][j];
            }
        }
        return Math.sqrt(2.0 * sum);
    }

    /**
     * The main parallel cyclic Jacobi eigenvalue method.
     * @param A Input symmetric matrix.
     * @return Pair of Matrices (Diagonal eigenvalue matrix D, and eigenvector matrix V).
     */

    /**
     * Computes the Frobenius norm of the matrix.
     *
     * @return the Euclidean (Frobenius) norm of the matrix
     */
    public double norm() {
        double sum = 0;
        for (int i = 0; i < getHeight(); i++) {
            for (int j = 0; j < getWidth(); j++) {
                sum += values[i][j] * values[i][j];
            }
        }
        return Math.sqrt(sum);
    }

    /**
     * Creates a deep copy of this {@code Matrix}.
     *
     * @return a clone of the current matrix
     */
    public Matrix clone() {
        Matrix m = new Matrix(this);
        return m;
    }

    /**
     * Returns the transpose of the given matrix.
     *
     * @param m the matrix to transpose
     * @return a new {@code Matrix} that is the transpose of {@code m}
     */
    public static Matrix transpose(Matrix m) {
        Matrix newm = m.clone();
        double[][] temp = newm.transposed;
        newm.transposed = newm.values;
        newm.values = temp;
        return newm;
    }

    /**
     * Returns the transpose of this matrix.
     *
     * @return a new {@code Matrix} that is the transpose of this matrix
     */
    public Matrix transpose() {
        return Matrix.transpose(this);
    }

    /**
     * Helper method to transpose a two-dimensional array.
     *
     * @param values the array to transpose
     * @return the transposed two-dimensional array
     */
    private static double[][] transpose2DArray(double[][] values) {
        int rows = values.length;
        int cols = values[0].length;
        double[][] transposed = new double[cols][rows];

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < rows; i++) {
            final int row = i;
            futures.add(executor.submit(() -> {
                for (int j = 0; j < cols; j++) {
                    transposed[j][row] = values[row][j];
                }
            }));
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        executor.shutdown();
        return transposed;
    }

    /**
     * Creates a zeros matrix with the specified dimensions.
     *
     * @param height the number of rows
     * @param width  the number of columns
     * @return a {@code Matrix} filled with zeros
     */
    public static Matrix zeros(int height, int width) {
        double[][] values = new double[height][width];
        Matrix newm = new Matrix(values);
        return newm;
    }

    /**
     * Adds two matrices.
     *
     * @param m1 the first matrix
     * @param m2 the second matrix
     * @return a new {@code Matrix} that is the sum of {@code m1} and {@code m2}
     * @throws ArithmeticException if the dimensions of the matrices do not match
     */
    public static Matrix add(Matrix m1, Matrix m2) {
        return m1.add(m2);
    }

    /**
     * Adds another matrix to this matrix.
     *
     * @param other the matrix to add
     * @return a new {@code Matrix} that is the sum of this matrix and {@code other}
     * @throws ArithmeticException if the dimensions of the matrices do not match
     */
    public Matrix add(Matrix other) {
        if ((other.values.length != this.values.length) || (other.values[0].length != this.values[0].length)) {
            throw new ArithmeticException("com.example.pcimg.Matrix's dimensions do not match");
        }
        Matrix result = new Matrix(this);
        for (int i = 0; i < other.getHeight(); i++) {
            for (int j = 0; j < other.getWidth(); j++) {
                result.values[i][j] += other.values[i][j];
            }
        }
        result.transposed = transpose2DArray(result.values);
        return result;
    }

    /**
     * Subtracts one matrix from another.
     *
     * @param m1 the first matrix
     * @param m2 the matrix to subtract from the first
     * @return a new {@code Matrix} that is the result of {@code m1 - m2}
     * @throws ArithmeticException if the dimensions of the matrices do not match
     */
    public static Matrix subtract(Matrix m1, Matrix m2) {
        return m1.subtract(m2);
    }

    /**
     * Subtracts another matrix from this matrix.
     *
     * @param other the matrix to subtract
     * @return a new {@code Matrix} that is the result of subtracting {@code other} from this matrix
     * @throws ArithmeticException if the dimensions of the matrices do not match
     */
    public Matrix subtract(Matrix other) {
        if ((other.values.length != this.values.length) || (other.values[0].length != this.values[0].length)) {
            throw new ArithmeticException("com.example.pcimg.Matrix's dimensions do not match");
        }
        Matrix result = new Matrix(this);
        for (int i = 0; i < other.getHeight(); i++) {
            for (int j = 0; j < other.getWidth(); j++) {
                result.values[i][j] -= other.values[i][j];
            }
        }
        result.transposed = transpose2DArray(result.values);
        return result;
    }

    /**
     * Computes the Hadamard (element-wise) product of this matrix and another matrix.
     *
     * @param m2 the other matrix
     * @return a new {@code Matrix} representing the Hadamard product
     */
    public Matrix hadamard(Matrix m2) {
        double[][] arr = new double[m2.getHeight()][m2.getWidth()];
        Matrix mulm = new Matrix(arr);
        for (int i = 0; i < getHeight(); i++) {
            for (int j = 0; j < getWidth(); j++) {
                mulm.values[i][j] = this.values[i][j] * m2.values[i][j];
            }
        }
        return mulm;
    }

    /**
     * Multiplies this matrix by a scalar.
     *
     * @param f the scalar to multiply by
     * @return a new {@code Matrix} resulting from the scalar multiplication
     */
    public Matrix dot(double f) {
        Matrix result = new Matrix(this);
        int h = result.getHeight();
        int w = result.getWidth();
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                double scaledValue = result.values[i][j] * f;
                result.values[i][j] = scaledValue;
                result.transposed[j][i] = scaledValue;
            }
        }
        return result;
    }

    /**
     * Multiplies a matrix by a scalar.
     *
     * @param m the matrix to scale
     * @param f the scalar value
     * @return a new {@code Matrix} resulting from the scalar multiplication
     */
    public static Matrix dot(Matrix m, double f) {
        return m.dot(f);
    }

    /**
     * Computes the dot product (matrix multiplication) of two matrices.
     *
     * @param m1 the left matrix operand
     * @param m2 the right matrix operand
     * @return a new {@code Matrix} that is the product of {@code m1} and {@code m2}
     * @throws ArithmeticException if the number of columns of {@code m1} does not equal the number of rows of {@code m2}
     */
    public static Matrix dot(Matrix m1, Matrix m2) {
        return m1.dot(m2);
    }

    /**
     * Computes the dot product (matrix multiplication) of this matrix with another matrix.
     *
     * @param m the matrix to multiply with
     * @return a new {@code Matrix} that is the product of this matrix and {@code m}
     * @throws ArithmeticException if the dimensions do not align for multiplication
     */
    public Matrix dot(Matrix m) {
        if (this.getWidth() != m.getHeight()) {
            throw new ArithmeticException("Matrices can't be multiplied");
        }
        int M = this.getHeight();
        int N = m.getWidth();
        int K = this.getWidth();
        Matrix result = Matrix.zeros(M, N);
        int blockSize = 64;
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Future<?>> futures = new ArrayList<>();

        for (int i0 = 0; i0 < M; i0 += blockSize) {
            int iStart = i0;
            int iEnd = Math.min(i0 + blockSize, M);
            Future<?> future = executor.submit(() -> {
                for (int k0 = 0; k0 < K; k0 += blockSize) {
                    int kEnd = Math.min(k0 + blockSize, K);
                    for (int j0 = 0; j0 < N; j0 += blockSize) {
                        int jEnd = Math.min(j0 + blockSize, N);
                        for (int i = iStart; i < iEnd; i++) {
                            for (int k = k0; k < kEnd; k++) {
                                double aVal = this.values[i][k];
                                for (int j = j0; j < jEnd; j++) {
                                    result.values[i][j] += aVal * m.values[k][j];
                                }
                            }
                        }
                    }
                }
            });
            futures.add(future);
        }

        for (Future<?> f : futures) {
            try {
                f.get();
            } catch (Exception e) {
                executor.shutdownNow();
                throw new RuntimeException(e);
            }
        }
        executor.shutdown();
        result.transposed = transpose2DArray(result.values);
        return result;
    }

    /**
     * Flattens the matrix into a single row.
     *
     * @return a new {@code Matrix} with one row and the number of columns equal to the total elements in the original matrix
     */
    public Matrix flatten() {
        double[][] arr = new double[1][getWidth() * getHeight()];
        int count = 0;
        for (int i = 0; i < getHeight(); i++) {
            for (int j = 0; j < getWidth(); j++) {
                arr[0][count] = this.values[i][j];
                count++;
            }
        }
        return new Matrix(arr);
    }

    /**
     * Sets the value of a specific element in the matrix.
     *
     * @param y     the row index
     * @param x     the column index
     * @param value the new value to set
     */
    public void set(int y, int x, double value) {
        values[y][x] = value;
        transposed[x][y] = value;
    }

    /**
     * Reshapes the matrix to the specified width and height.
     *
     * @param width  the desired number of columns
     * @param height the desired number of rows
     * @return a new {@code Matrix} with the reshaped dimensions
     */
    public Matrix reshape(int width, int height) {
        Matrix m = Matrix.zeros(height, width);
        double[] arr = new double[width * height];
        int count = 0;
        for (int i = 0; i < getHeight(); i++) {
            for (int j = 0; j < getWidth(); j++) {
                arr[count] = this.values[i][j];
                count++;
            }
        }
        count = 0;
        double[][] newarr = new double[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                newarr[i][j] = arr[count];
                count++;
            }
        }
        m.setValues(newarr);
        return m;
    }

    /**
     * Returns the number of rows in the matrix.
     *
     * @return the row count
     */
    public int getRowCount() {
        return getHeight();
    }

    /**
     * Returns the number of columns in the matrix.
     *
     * @return the column count
     */
    public int getColumnCount() {
        return getWidth();
    }

    /**
     * Constructs a {@code Matrix} of zeros with the specified dimensions.
     *
     * @param rows the number of rows
     * @param cols the number of columns
     */
    public Matrix(int rows, int cols) {
        this.values = new double[rows][cols];
        this.transposed = transpose2DArray(this.values);
    }

    /**
     * Returns a submatrix obtained by dropping the first {@code k} rows.
     *
     * @param k the number of rows to drop
     * @return a new {@code Matrix} consisting of all columns and rows starting from index {@code k}
     */
    public Matrix minor(int k) {
        int newRows = this.getHeight() - k;
        int cols = this.getWidth();
        double[][] sub = new double[newRows][cols];
        for (int i = 0; i < newRows; i++) {
            System.arraycopy(this.values[i + k], 0, sub[i], 0, cols);
        }
        return new Matrix(sub);
    }

    /**
     * Returns the specified column as a column vector.
     *
     * @param col the column index to extract
     * @return a new {@code Matrix} representing the column vector
     */
    public Matrix column(int col) {
        int rows = this.getHeight();
        double[][] colArr = new double[rows][1];
        for (int i = 0; i < rows; i++) {
            colArr[i][0] = this.values[i][col];
        }
        return new Matrix(colArr);
    }

    /**
     * Computes the Euclidean norm (magnitude) of a column vector.
     *
     * @return the magnitude of the column vector
     * @throws RuntimeException if the matrix is not a column vector
     */
    public double magnitude() {
        if (this.getWidth() != 1) {
            throw new RuntimeException("magnitude() is defined only for column vectors.");
        }
        double sum = 0.0;
        for (int i = 0; i < this.getHeight(); i++) {
            double val = this.values[i][0];
            sum += val * val;
        }
        return Math.sqrt(sum);
    }

    /**
     * Multiplies the matrix by a scalar.
     * This is an alias for {@link #dot(double)}.
     *
     * @param f the scalar multiplier
     * @return a new {@code Matrix} resulting from the multiplication
     */
    public Matrix scalarMultiply(double f) {
        return this.dot(f);
    }

    /**
     * Returns a normalized (unit) version of a column vector.
     *
     * @return a new {@code Matrix} representing the normalized column vector;
     *         if the magnitude is zero, the original matrix is returned
     */
    public Matrix unit() {
        double norm = this.magnitude();
        if (norm == 0) return this;
        return this.dot(1.0 / norm);
    }

    /**
     * Constructs the Householder reflection matrix H = I - 2*u*u^T for a given column vector u.
     *
     * @param u a column vector used to construct the Householder reflection
     * @return the Householder reflection matrix
     */
    public static Matrix householderFactor(Matrix u) {
        int n = u.getHeight();
        Matrix H = Matrix.eye(n);
        Matrix uuT = Matrix.zeros(n, n);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                uuT.values[i][j] = u.values[i][0] * u.values[j][0];
            }
        }
        return H.subtract(uuT.dot(2.0));
    }

    /**
     * Computes a Householder-based QR decomposition of the matrix.
     * This method is used internally by {@link #eigenQR(Matrix)}.
     *
     * @param A the matrix to decompose
     * @return a {@link Pair} where the first element is the upper triangular matrix R and
     *         the second element is the orthogonal matrix Q such that A = Q * R
     */
    private static Pair householderQR(Matrix A) {
        int m = A.getRowCount();
        int n = A.getColumnCount();
        Matrix R = A.clone();
        Matrix Q_total = Matrix.eye(m);

        for (int k = 0; k < Math.min(n, m - 1); k++) {
            Matrix x = new Matrix(R.minor(k).column(k).values);
            double normX = x.magnitude();
            if (R.get(k, k) > 0) {
                normX = -normX;
            }
            Matrix e = Matrix.zeros(x.getHeight(), 1);
            e.set(0, 0, 1);
            Matrix u = e.scalarMultiply(normX).add(x).unit();
            Matrix H_sub = householderFactor(u);
            Matrix H = Matrix.eye(m);
            for (int i = k; i < m; i++) {
                for (int j = k; j < m; j++) {
                    H.set(i, j, H_sub.values[i - k][j - k]);
                }
            }
            R = H.dot(R);
            Q_total = Q_total.dot(H);
        }
        return new Pair(R, Q_total.transpose());
    }

    /**
     * Computes the eigenvalues and eigenvectors of a matrix using the QR algorithm with Householder QR.
     * Iteratively computes the QR decomposition and updates the matrices until convergence.
     *
     * @param A the matrix for which eigenvalues and eigenvectors are to be computed
     * @return a {@link Pair} where the first element is a diagonal matrix of eigenvalues and the
     *         second element is the matrix of eigenvectors
     */
    public static Pair eigenQR(Matrix A) {
        int n = A.getHeight();
        Matrix Q_total = Matrix.eye(n);
        Matrix A_iter = A.clone();
        int maxIter = 20;
        double tol = 1e-10;

        for (int iter = 0; iter < maxIter; iter++) {
            System.out.println("Starting iteration " + iter);
            Pair qr = householderQR(A_iter);
            System.out.println("househeld");
            Matrix Q = qr.second;
            Matrix R = qr.first;
            A_iter = R.dot(Q);
            Q_total = Q_total.dot(Q);
        }

        double[][] dArr = new double[n][n];
        for (int i = 0; i < n; i++) {
            dArr[i][i] = A_iter.get(i, i);
        }
        Matrix D = new Matrix(dArr);
        return new Pair(D, Q_total);
    }

    /**
     * Saves the matrix to a file using serialization.
     *
     * @param filename the file path to save the matrix
     * @throws IOException if an I/O error occurs while writing to the file
     */
    public void saveToFile(String filename) throws IOException {
        File file = new File(filename);
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(this);
            System.out.println("Matrix saved to file: " + filename);
        }
    }

    /**
     * Loads a matrix from a file using serialization.
     *
     * @param filename the file path from which to load the matrix
     * @return the {@code Matrix} loaded from the file
     * @throws IOException            if an I/O error occurs while reading the file
     * @throws ClassNotFoundException if the class of the serialized object cannot be found
     */
    public static Matrix loadFromFile(String filename)
            throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            Matrix m = (Matrix) ois.readObject();
            System.out.println("Matrix loaded from file: " + filename);
            return m;
        }
    }
}
