package mclaudio76.multitenantjpa;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jta.atomikos.AtomikosDataSourceBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import com.mysql.cj.jdbc.MysqlXADataSource;

import mclaudio76.multitenantjpa.tenant.TenantInterceptor;

@Configuration
public class ApplicationConfiguration {
	
   @Autowired
   ApplicationContext context;
	
   @Bean
   @DependsOn("JTATXManager")
   @Primary
   public EntityManagerFactory entityManagerFactory(@Qualifier("hibernate-props") Properties properties) {
	    RoutingDatasource routingDS = getRoutingDS(TenantInterceptor.TENANT_A, TenantInterceptor.TENANT_B) ;
	    EntityManagerFactory alfa   = createEntityManagerFactory(properties, routingDS,"AppEntityManager");
	    return alfa;
   }
  
   
   @PostConstruct
   public void completeInitializationOfSpecificEntityManagers() {
	   GenericApplicationContext ctx 	   = (GenericApplicationContext) context;
	   ctx.registerBean(TenantInterceptor.TENANT_B, EntityManagerFactory.class, () -> prepareSpecificEntityManager(TenantInterceptor.TENANT_B,getHibernateProperties()));
   }
   
   private EntityManagerFactory prepareSpecificEntityManager(String tenantID, Properties properties) {
	   RoutingDatasource routingDS = getRoutingDS(tenantID) ;
	   EntityManagerFactory alfa   = createEntityManagerFactory(properties, routingDS,tenantID);
       return alfa;
   }
   
   
   /**
    * Here datasources are hard-coded, nothing prevents to load them from an external configuration.
    * 
    */
   private RoutingDatasource getRoutingDS(String ... tenants) {
	   RoutingDatasource ds = new RoutingDatasource();
	   Map<Object, Object> availDS = new HashMap<>();
	   String defaultTenant		   = null;
	   if(tenants != null) {
		   defaultTenant  = tenants[0];
		   List<String> requestes = Arrays.asList(tenants);
		   if(requestes.contains(TenantInterceptor.TENANT_A)) {
			   availDS.put(TenantInterceptor.TENANT_A, buildDataSource("XADS1","jdbc:mysql://localhost:3306/mydatabase", "dbuser",  "dbuser"));
		   }
		   if(requestes.contains(TenantInterceptor.TENANT_B)) {
			   availDS.put(TenantInterceptor.TENANT_B, buildDataSource("XADS2","jdbc:mysql://localhost:3306/anotherDB",  "secuser", "secuser"));
		   }
	   }
	   else {
		   defaultTenant  = TenantInterceptor.TENANT_A;
		   availDS.put(TenantInterceptor.TENANT_A, buildDataSource("XADS1","jdbc:mysql://localhost:3306/mydatabase", "dbuser",  "dbuser"));
	       availDS.put(TenantInterceptor.TENANT_B, buildDataSource("XADS2","jdbc:mysql://localhost:3306/anotherDB",  "secuser", "secuser"));
	   }
	   ds.setTargetDataSources(availDS);
	   ds.setDefaultTargetDataSource(availDS.get(defaultTenant));
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
   
   private EntityManagerFactory createEntityManagerFactory(Properties properties, RoutingDatasource routingDS, String unitName) {
       LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
       em.setDataSource(routingDS);
       em.setPackagesToScan(new String[] { "mclaudio76.multitenantjpa.entities" });
       JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
       em.setJpaVendorAdapter(vendorAdapter);
       em.setPersistenceUnitName(unitName);
       em.setJpaProperties(properties);
       em.afterPropertiesSet();
       EntityManagerFactory emf = em.getNativeEntityManagerFactory(); 
       return emf;
   }
	
}
