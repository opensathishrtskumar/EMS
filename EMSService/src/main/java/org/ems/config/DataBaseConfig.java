
package org.ems.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DataBaseConfig {

	@Autowired
	@Qualifier("basicDataSource")
	private DataSource dataSource;

	/**
	 * Allows repositories to access RDBMS data using the JDBC API.
	 */
	@Bean
	@DependsOn("basicDataSource")
	public JdbcTemplate jdbcTemplate() {
		return new JdbcTemplate(dataSource);
	}
}
