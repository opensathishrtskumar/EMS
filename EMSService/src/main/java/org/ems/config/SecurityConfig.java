/*
 * Copyright 2010-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ems.config;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ems.util.EMSPasswordEncoder;

@Configuration
public class SecurityConfig {

	@Inject
	private Environment environment;

	/*@Bean
	public PasswordEncoder passwordEncoder() {
		return new EMSPasswordEncoder(getEncryptPassword());
	}

	@Bean
	public TextEncryptor textEncryptor() {
		return Encryptors.queryableText(getEncryptPassword(), environment.getProperty("security.encryptSalt"));
	}

	// helpers
	private String getEncryptPassword() {
		return environment.getProperty("security.encryptPassword");
	}*/
}