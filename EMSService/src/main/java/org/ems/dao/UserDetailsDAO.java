package org.ems.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.inject.Inject;

import org.ems.model.Account;
import org.ems.model.ChangePasswordForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.ems.security.Security;

@Repository
public class UserDetailsDAO {

	private static final Logger logger = LoggerFactory.getLogger(UserDetailsDAO.class);

	private final JdbcTemplate jdbcTemplate;

	private static final String AUTHENTICATE = "SELECT id,username,email,firstname,lastname FROM setup.user_credential WHERE username = ? and credential=?";
	
	private static final String UPDATE_PASSWORD = "update setup.user_credential set credential = ? where id=?";
	

	@Autowired
	private Security security;

	@Inject
	public UserDetailsDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public Account authenticate(final String username, final String password) throws Exception {
		try {
			String encryptedPassword = security.encrypt(password);
			logger.trace("Encrypted password {}", encryptedPassword);

			return jdbcTemplate.queryForObject(AUTHENTICATE, new String[] { username, encryptedPassword },
					new RowMapper<Account>() {
						@Override
						public Account mapRow(ResultSet rs, int rowNum) throws SQLException {
							logger.trace("row index in authenticate :  {}", rowNum);
							// Load any other information that needs to be in session
							return new Account(rs.getLong("id"), rs.getString("firstname"), rs.getString("lastname"),
									rs.getString("email"), rs.getString("username"))
											.setEncryptedPassword(encryptedPassword);
						}
					});

		} catch (Exception e) {
			logger.error("authenticating exception {}", e);
			throw e;
		}
	}
	
	public int updatePassword(ChangePasswordForm form) {
		return jdbcTemplate.update(UPDATE_PASSWORD, new Object[]{form.getConfirmPassword(),form.getId()});
	}
	
}
