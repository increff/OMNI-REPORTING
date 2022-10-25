package com.increff.omni.reporting.config;

import com.increff.commons.sql.DbPoolUtil;
import com.nextscm.commons.spring.server.SnakeCaseNamingStrategy;
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

	private static final int CONNECTION_POOL_SIZE = 12;

	public static final String PACKAGE_POJO = "com.increff.omni.reporting.pojo";

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
	@Value("${hibernate.jdbc.time_zone}")
	private String hibernateTimezone;


	@Bean(name = "dataSource")
	public DataSource getDataSource() {
		return DbPoolUtil.initDataSource(jdbcDriver, jdbcUrl, jdbcUsername, jdbcPassword, CONNECTION_POOL_SIZE,
				(int) (CONNECTION_POOL_SIZE * 1.5));
	}

	@Bean(name = "entityManagerFactory")
	@Autowired
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
		LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();
		bean.setDataSource(dataSource);
		bean.setPackagesToScan(PACKAGE_POJO);
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
