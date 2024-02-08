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

	private static final int CONNECTION_POOL_SIZE = 50;

	public static final String PACKAGE_POJO = "com.increff.omni.reporting.pojo";
	public static final String AUDIT_POJO = "com.nextscm.commons.spring.audit.pojo";

	@Autowired
	private ApplicationProperties properties;



	@Bean(name = "dataSource")
	public DataSource getDataSource() {
		return DbPoolUtil.initDataSource(properties.getJdbcDriver(), properties.getJdbcUrl(), properties.getJdbcUsername(),
				properties.getJdbcPassword(), properties.getMinConnection(), properties.getMaxConnection());
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
		jpaProperties.put("hibernate.dialect", properties.getHibernateDialect());
		jpaProperties.put("hibernate.show_sql", properties.getHibernateShowSql());
		jpaProperties.put("hibernate.hbm2ddl.auto", properties.getHibernateHbm2ddl());
		jpaProperties.put("hibernate.jdbc.time_zone", properties.getHibernateTimezone());
		jpaProperties.put("hibernate.jdbc.batch_size", properties.getHibernateJdbcBatchSize());
		jpaProperties.put("hibernate.cache.use_second_level_cache", false);
		jpaProperties.put("hibernate.physical_naming_strategy", new SnakeCaseNamingStrategy(""));
		jpaProperties.put("hibernate.id.generator.stored_last_used", properties.getHibernateIdGeneratorStoredLastUsed());
		jpaProperties.put("hibernate.model.generator_name_as_sequence_name", properties.getHibernateModelGeneratorNameAsSequenceName());
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
