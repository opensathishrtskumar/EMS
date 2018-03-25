package org.ems.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.inject.Inject;

import org.ems.model.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class UserDetailsDAO {

	private static final Logger logger = LoggerFactory.getLogger(UserDetailsDAO.class);

	private final JdbcTemplate jdbcTemplate;

	private static final String AUTHENTICATE = "SELECT id,username,email,firstname,lastname FROM setup.user_credential WHERE username = ? and credential=?";

	@Inject
	public UserDetailsDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public Account authenticate(final String username, final String password) throws Exception {
		try {

			return jdbcTemplate.queryForObject(AUTHENTICATE, new String[] { username, password },
					new RowMapper<Account>() {
						@Override
						public Account mapRow(ResultSet rs, int rowNum) throws SQLException {
							logger.trace("row index in authenticate :  {}", rowNum);
							return new Account(rs.getLong("id"), rs.getString("firstname"), rs.getString("lastname"),
									rs.getString("email"), rs.getString("username"));
						}
					});

		} catch (Exception e) {
			logger.error("authenticating exception {}", e);
			throw e;
		}
	}

}
