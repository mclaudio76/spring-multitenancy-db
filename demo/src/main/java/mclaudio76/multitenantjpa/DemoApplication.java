package mclaudio76.multitenantjpa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.XADataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(exclude={DataSourceAutoConfiguration.class,XADataSourceAutoConfiguration.class,HibernateJpaAutoConfiguration.class},
	scanBasePackages= {"mclaudio76.multitenantjpa"})
@EnableTransactionManagement

public class DemoApplication {

	public static void main(String[] args) {
		System.getProperties().put("com.atomikos.icatch.default_jta_timeout", "127000");
		SpringApplication.run(DemoApplication.class, args);
	}

}
