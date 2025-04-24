package com.example.pcimg;// SQL to create the table:
// --------------------------------------------------
// CREATE TABLE IF NOT EXISTS pca_models (
//     name TEXT PRIMARY KEY,
//     model BYTEA NOT NULL
// );

import com.example.pcimg.PCA;

import java.sql.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for storing and retrieving PCA models in PostgreSQL.
 */
public class pcadb {
    private final Connection conn;

    public pcadb(Connection conn) {
        this.conn = conn;
    }

    /**
     * Creates the pca_models table if it doesn't already exist.
     */
    public void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS pca_models ("
                + "name TEXT PRIMARY KEY,"
                + "model BYTEA NOT NULL)";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    /**
     * Inserts or updates a PCA object under the given name.
     */
    public void savePCA(String name, PCA pca) throws SQLException, IOException {
        String sql = "INSERT INTO pca_models(name, model) VALUES (?, ?) "
                + "ON CONFLICT (name) DO UPDATE SET model = EXCLUDED.model";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {

            oos.writeObject(pca);
            oos.flush();
            byte[] pcaBytes = bos.toByteArray();

            ps.setString(1, name);
            ps.setBytes(2, pcaBytes);
            ps.executeUpdate();
        }
    }

    /**
     * Loads and deserializes the PCA object stored under the given name.
     *
     * @return the PCA object, or null if not found
     */
    public PCA loadPCA(String name) throws SQLException, IOException, ClassNotFoundException {
        String sql = "SELECT model FROM pca_models WHERE name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                byte[] pcaBytes = rs.getBytes("model");
                try (ByteArrayInputStream bis = new ByteArrayInputStream(pcaBytes);
                     ObjectInputStream ois = new ObjectInputStream(bis)) {
                    return (PCA) ois.readObject();
                }
            }
        }
    }

    public List<String> list() throws SQLException {
        String sql = "SELECT name FROM pca_models ORDER BY name";
        List<String> names = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                names.add(rs.getString("name"));
            }
        }
        return names;
    }
    public static void main(String[] args) throws Exception{

        String url="jdbc:postgresql://localhost:5432/yippe";
        String userName="postgres";
        String password="postgres";
        try (Connection conn = DriverManager.getConnection(url, userName, password)){
            pcadb db=new pcadb(conn);
            db.createTable();
            System.out.println("yippe");

        }



    }

    public void importPCAFromFile(String name, String filePath)
            throws IOException, ClassNotFoundException, SQLException {
        PCA pca;
        try (FileInputStream fis = new FileInputStream(filePath);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            pca = (PCA) ois.readObject();
        }
        savePCA(name, pca);
    }
    public void deletePCA(String name) throws SQLException {
        String sql = "DELETE FROM pca_models WHERE name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.executeUpdate();
        }
    }
}


