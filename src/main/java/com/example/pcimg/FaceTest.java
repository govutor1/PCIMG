package com.example.pcimg;

import java.sql.Connection;
import java.sql.DriverManager;

public class FaceTest {
    public static void main(String[] args) {
        try {
            String url  = "jdbc:postgresql://localhost:5432/yippe";
            String user = "postgres";
            String pass = "3280";
            Connection con = DriverManager.getConnection(url, user, pass);
            pcadb db = new pcadb(con);
            db.createTable();
            db.importPCAFromFile("faces", "PCAMagnivFile");
            db.deletePCA("sex");


        } catch (Exception ex) {
            throw new RuntimeException("Failed to initialize PCA spinner", ex);
        }
    }

}