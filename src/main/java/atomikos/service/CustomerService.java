package atomikos.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Service
public class CustomerService {

    @Autowired
    private DataSource dataSource;

    @Transactional
    public void selectForUpdate(Long id) throws SQLException {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            String selectSql = "SELECT * FROM customer WHERE id = ? FOR UPDATE";
            PreparedStatement selectStmt = connection.prepareStatement(selectSql);
            selectStmt.setLong(1, id);
            ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) {
                System.out.println("Lock acquired on record with ID: " + id);
                // Simulate long-running transaction
                Thread.sleep(5000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    @Transactional
    public void updateCustomer(Long id, double newCreditLimit) throws SQLException {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            String updateSql = "UPDATE customer SET credit_limit = ? WHERE id = ?";
            PreparedStatement updateStmt = connection.prepareStatement(updateSql);
            updateStmt.setDouble(1, newCreditLimit);
            updateStmt.setLong(2, id);
            int rows = updateStmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Updated customer with ID: " + id);
            }
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }
}
