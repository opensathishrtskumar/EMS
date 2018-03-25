package org.ems.config;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.ems.dao.UserDetailsDAO;
import org.ems.model.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;

public class UsernamePasswordAuthenticationProvider implements AuthenticationProvider {

	private static final Logger logger = LoggerFactory.getLogger(UsernamePasswordAuthenticationProvider.class);

	private UserDetailsDAO accountRepository;

	@PostConstruct
	public void init() {
		logger.info("UsernamePasswordAuthentication initialized");
	}

	@Inject
	public UsernamePasswordAuthenticationProvider(UserDetailsDAO accountRepository) {
		this.accountRepository = accountRepository;
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;
		try {
			Account account = accountRepository.authenticate(token.getName(), (String) token.getCredentials());
			return authenticatedToken(account, authentication);
		} catch (Exception e) {
			logger.error("{}", e);
			throw new org.springframework.security.core.userdetails.UsernameNotFoundException(token.getName(), e);
		}
	}

	@Override
	public boolean supports(Class<? extends Object> authentication) {
		return UsernamePasswordAuthenticationToken.class.equals(authentication);
	}

	private Authentication authenticatedToken(Account account, Authentication original) {
		List<GrantedAuthority> authorities = null;
		UsernamePasswordAuthenticationToken authenticated = new UsernamePasswordAuthenticationToken(account, null,
				authorities);
		authenticated.setDetails(original.getDetails());
		return authenticated;
	}

}