package io.demo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PostgresSelectForUpdateExample {

    // Database credentials and connection URL
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/customerdb";
    private static final String USER = "admin";
    private static final String PASS = "admin";


    // Iterate through the result set and print details
    public void print(String type,ResultSet rs) throws SQLException {
        while (rs.next()) {
            int id = rs.getInt("id");
            Double credit_limit = rs.getDouble("credit_limit");
            System.out.println("type : " + type + " id: " + id + ", credit_limit : " + credit_limit );
        }
    }

    public void select(int id) throws InterruptedException {
        Connection conn = null;
        PreparedStatement stmt = null;
        PreparedStatement updateStmt = null;
        ResultSet rs = null;

        Thread.sleep(5000); // Simulate processing time
        try {
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            //conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);

            // Start transaction
            conn.setAutoCommit(false);

            // Execute SELECT ... FOR UPDATE query
            String sql = "SELECT * FROM customer WHERE id = ? FOR UPDATE";
            stmt = conn.prepareStatement(sql);
            stmt.setInt((int)1,id);
            rs = stmt.executeQuery();

            print("READ",rs);
            // Commit transaction
            conn.commit();
            System.out.println("Transaction Completed.");

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (conn != null) {
                    conn.rollback(); // Rollback transaction on exception
                    System.out.println("Transaction rolled back.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            // Close resources
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Method to perform SELECT ... FOR UPDATE
    public void selectForUpdate(int id) {
        Connection conn = null;
        PreparedStatement stmt = null;
        PreparedStatement updateStmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // Start transaction
            conn.setAutoCommit(false);
            //conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);

            // Execute SELECT ... FOR UPDATE query
            String sql = "SELECT * FROM customer WHERE id = ? FOR UPDATE";
            stmt = conn.prepareStatement(sql);
            stmt.setInt((int)1,id);
            rs = stmt.executeQuery();

            print("UPDATE",rs);;

            // Simulate processing
            System.out.println("Acquired lock for record with id " + id);

            // Execute UPDATE statement
            String updateSql = "UPDATE customer SET credit_limit = 250.00 WHERE id = ?";
            updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setInt((int)1, id);
            updateStmt.executeUpdate();
            System.out.println("Update performed for record with id " + id);

            System.out.println("Waiting before commit ..." + id);
            Thread.sleep(10000); // Simulate processing time

            // Commit transaction
            conn.commit();
            System.out.println("Transaction committed.");

        } catch (SQLException | InterruptedException e) {
            e.printStackTrace();
            try {
                if (conn != null) {
                    conn.rollback(); // Rollback transaction on exception
                    System.out.println("Transaction rolled back.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            // Close resources
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        PostgresSelectForUpdateExample example = new PostgresSelectForUpdateExample();

        // Thread 1: Perform SELECT ... FOR UPDATE
        Thread thread1 = new Thread(() -> {
            example.selectForUpdate(1);
        });

        // Thread 2: Attempt to read the same record (will wait until lock is released)
        Thread thread2 = new Thread(() -> {
            try {
                example.select(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        // Start threads
        thread1.start();
        thread2.start();
    }
}
