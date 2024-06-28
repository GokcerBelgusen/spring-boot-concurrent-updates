package io.demo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PostgresAdvisoryLockExample {

    // Database credentials and connection URL
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/customerdb";
    private static final String USER = "admin";
    private static final String PASS = "admin";


    // Method to perform SELECT ... FOR UPDATE with advisory lock
    public void selectForUpdateWithAdvisoryLock(int id) {
        Connection conn = null;
        PreparedStatement selectStmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // Start transaction
            conn.setAutoCommit(false);

            // Acquire advisory lock
            boolean advisoryLockAcquired = acquireAdvisoryLock(conn, id);
            if (!advisoryLockAcquired) {
                System.out.println("Failed to acquire advisory lock. Exiting.");
                return;
            }

            // Execute SELECT ... FOR UPDATE query
            String selectSql = "SELECT * FROM customer WHERE id = ? FOR UPDATE";
            selectStmt = conn.prepareStatement(selectSql);
            selectStmt.setInt(1, id);
            rs = selectStmt.executeQuery();

            // Simulate processing
            System.out.println("Thread 1 acquired lock and advisory lock for record with id " + id);
            Thread.sleep(5000); // Simulate processing time

            // Commit transaction
            conn.commit();
            System.out.println("Thread 1 committed transaction.");

        } catch (SQLException | InterruptedException e) {
            e.printStackTrace();
            try {
                if (conn != null) {
                    conn.rollback(); // Rollback transaction on exception
                    System.out.println("Thread 1 rolled back transaction.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            // Release advisory lock and close resources
            releaseAdvisoryLock(conn, id);
            try {
                if (rs != null) rs.close();
                if (selectStmt != null) selectStmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Method to acquire advisory lock
    private boolean acquireAdvisoryLock(Connection conn, int lockKey) {
        try {
            PreparedStatement lockStmt = conn.prepareStatement("SELECT pg_try_advisory_lock(?)");
            lockStmt.setInt(1, lockKey);
            ResultSet rs = lockStmt.executeQuery();
            if (rs.next() && rs.getBoolean(1)) {
                return true; // Lock acquired
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Failed to acquire lock
    }

    // Method to release advisory lock
    private void releaseAdvisoryLock(Connection conn, int lockKey) {
        try {
            PreparedStatement unlockStmt = conn.prepareStatement("SELECT pg_advisory_unlock(?)");
            unlockStmt.setInt(1, lockKey);
            unlockStmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        PostgresAdvisoryLockExample example = new PostgresAdvisoryLockExample();

        // Thread 1: Perform SELECT ... FOR UPDATE with advisory lock
        Thread thread1 = new Thread(() -> {
            example.selectForUpdateWithAdvisoryLock(1);
        });

        // Thread 2: Attempt to read data (without acquiring the advisory lock)
        Thread thread2 = new Thread(() -> {
            example.selectForUpdateWithAdvisoryLock(1); // Simulating a read attempt
        });

        // Start threads
        thread1.start();
        thread2.start();
    }
}
