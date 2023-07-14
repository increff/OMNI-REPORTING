package com.increff.omni.reporting.config;

import com.increff.omni.reporting.util.DbPoolUtil;
import com.increff.omni.reporting.util.SnakeCaseNamingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;


@Configuration
@EnableTransactionManagement
public class DbConfig {

	private static final int CONNECTION_POOL_SIZE = 50;

	public static final String PACKAGE_POJO = "com.increff.omni.reporting.pojo";
	public static final String AUDIT_POJO = "com.nextscm.commons.spring.audit.pojo";

	@Value("${jdbc.driverClassName:com.mysql.jdbc.Driver}")
	private String jdbcDriver;
	@Value("${jdbc.url}")
	private String jdbcUrl;
	@Value("${jdbc.username}")
	private String jdbcUsername;
	@Value("${jdbc.password}")
	private String jdbcPassword;
	@Value("${hibernate.dialect:org.hibernate.dialect.MySQLDialect}")
	private String hibernateDialect;
	@Value("${hibernate.show_sql:false}")
	private String hibernateShowSql;
	@Value("${hibernate.jdbc.batch_size:50}")
	private String hibernateJdbcBatchSize;
	@Value("${hibernate.hbm2ddl.auto}")
	private String hibernateHbm2ddl;
	@Value("${hibernate.jdbc.time_zone:UTC}")
	private String hibernateTimezone;
	@Value("${hibernate.min.connection:50}")
	private Integer minConnection;
	@Value("${hibernate.max.connection:100}")
	private Integer maxConnection;


	@Bean(name = "dataSource")
	public DataSource getDataSource() {
		return DbPoolUtil.initDataSource(jdbcDriver, jdbcUrl, jdbcUsername, jdbcPassword, minConnection,
				maxConnection);
	}

	@Bean(name = "entityManagerFactory")
	@Autowired
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
		LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();
		bean.setDataSource(dataSource);
		bean.setPackagesToScan(PACKAGE_POJO, AUDIT_POJO);
		HibernateJpaVendorAdapter jpaAdapter = new HibernateJpaVendorAdapter();
		bean.setJpaVendorAdapter(jpaAdapter);
		Properties jpaProperties = new Properties();
		jpaProperties.put("hibernate.dialect", hibernateDialect);
		jpaProperties.put("hibernate.show_sql", hibernateShowSql);
		jpaProperties.put("hibernate.hbm2ddl.auto", hibernateHbm2ddl);
		jpaProperties.put("hibernate.jdbc.time_zone", hibernateTimezone);
		jpaProperties.put("hibernate.jdbc.batch_size", hibernateJdbcBatchSize);
		jpaProperties.put("hibernate.cache.use_second_level_cache", false);
		jpaProperties.put("hibernate.physical_naming_strategy", new SnakeCaseNamingStrategy(""));
		jpaProperties.put("hibernate.id.db_structure_naming_strategy", "single");
		jpaProperties.put("hibernate.id.generator.stored_last_used", false);
		jpaProperties.put("hibernate.model.generator_name_as_sequence_name", false);
		jpaProperties.put("hibernate.default_storage_engine", "InnoDB");
//		jpaProperties.put("hibernate.id.new_generator_mappings",false);
		bean.setJpaProperties(jpaProperties);
		return bean;
	}

	@Bean(name = "transactionManager")
	@Autowired
	public JpaTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean emf) {
		JpaTransactionManager bean = new JpaTransactionManager();
		bean.setEntityManagerFactory(emf.getObject());
		return bean;
	}

}
