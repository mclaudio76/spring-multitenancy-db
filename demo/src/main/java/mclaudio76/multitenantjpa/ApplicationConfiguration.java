package mclaudio76.multitenantjpa;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jta.atomikos.AtomikosDataSourceBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import com.mysql.cj.jdbc.MysqlXADataSource;

import mclaudio76.multitenantjpa.tenant.TenantInterceptor;

@Configuration
public class ApplicationConfiguration {
   
	//Transaction manager must be instantiated BEFORE datasources are created.
   @Bean("ApplicationEntityManager")
   @DependsOn("JTATXManager")
   public EntityManagerFactory entityManagerFactory(@Qualifier("hibernate-props") Properties properties, RoutingDatasource routingDS) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(routingDS);
        em.setPackagesToScan(new String[] { "mclaudio76.multitenantjpa.entities" });
        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setPersistenceUnitName("AppEntityManager");
        em.setJpaProperties(properties);
        em.afterPropertiesSet();
        return em.getNativeEntityManagerFactory();
    }

   
   @Bean
   public RoutingDatasource getRoutingDS() {
	   RoutingDatasource ds = new RoutingDatasource();
	   Map<Object, Object> availDS = new HashMap<>();
	   availDS.put(TenantInterceptor.TENANT_A, buildDataSource("XADS1","jdbc:mysql://localhost:3306/mydatabase", "dbuser",  "dbuser"));
	   availDS.put(TenantInterceptor.TENANT_B, buildDataSource("XADS2","jdbc:mysql://localhost:3306/anotherDB",  "secuser", "secuser"));
	   ds.setTargetDataSources(availDS);
	   ds.setDefaultTargetDataSource(availDS.get(TenantInterceptor.TENANT_A));
	   ds.afterPropertiesSet();
	   return ds;
   }
   
   @Bean("hibernate-props")
   public Properties getHibernateProperties() {
		 Properties properties = new Properties();
        properties.setProperty("hibernate.hbm2ddl.auto", "update");
        properties.setProperty("hibernate.connection.autocommit", "false");
        properties.setProperty("hibernate.show_sql", "true");
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        // MOST IMPORTANT ! We need to tell Hibernate to use JTA as transaction coordinator.
        properties.setProperty("hibernate.transaction.coordinator_class", "jta");
        properties.setProperty("hibernate.transaction.factory_class","org.hibernate.transaction.JTATransactionFactory");
        properties.setProperty("hibernate.transaction.jta.platform", JTATXManager.class.getCanonicalName());
        return properties;
	}
   
   private DataSource buildDataSource(String dataSourceID, String url, String user, String pwd) {
		try {
			MysqlXADataSource mysqlXaDataSource = new MysqlXADataSource();
			mysqlXaDataSource.setUrl(url);
			mysqlXaDataSource.setPinGlobalTxToPhysicalConnection(true);
			mysqlXaDataSource.setPassword(pwd);
			mysqlXaDataSource.setUser(user);
			AtomikosDataSourceBean wrapper = new AtomikosDataSourceBean();
			wrapper.setUniqueResourceName(dataSourceID);
			wrapper.setMaxPoolSize(100);
			wrapper.setMinPoolSize(1);
			wrapper.setXaDataSource(mysqlXaDataSource); 
			return wrapper; 
		}
		catch(Exception e) {
			return null;
		}
	}
	
}
