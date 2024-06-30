package atomikos.config;

import org.postgresql.xa.PGXADataSource;
import org.springframework.boot.jta.atomikos.AtomikosDataSourceBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

@Configuration
@EnableTransactionManagement

public class DataSourceConfig {

    @Bean
    public AtomikosDataSourceBean dataSource() {
        PGXADataSource xaDataSource = new PGXADataSource();
        xaDataSource.setUrl("jdbc:postgresql://localhost:5432/customerdb");
        xaDataSource.setUser("admin");
        xaDataSource.setPassword("admin");

        AtomikosDataSourceBean dataSourceBean = new AtomikosDataSourceBean();
        dataSourceBean.setXaDataSource(xaDataSource);
        dataSourceBean.setUniqueResourceName("customerdbabc");
        dataSourceBean.setPoolSize(5);

        return dataSourceBean;
    }

    @Bean
    public JtaTransactionManager transactionManager() {
        UserTransaction userTransaction = new com.atomikos.icatch.jta.UserTransactionImp();
        TransactionManager transactionManager = new com.atomikos.icatch.jta.UserTransactionManager();

        return new JtaTransactionManager(userTransaction, transactionManager);
    }
}

