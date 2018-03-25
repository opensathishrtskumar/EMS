package org.ems.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Account implements Serializable {

	private final Long id;

	private final String firstName;

	private final String lastName;

	private final String email;

	private final String username;

	/**
	 * Constructs an Account object. The profielUrl parameter is a UriTemplate to
	 * allow the URL to be re-generated if the Account is updated.
	 */
	public Account(Long id, String firstName, String lastName, String email, String username) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.username = username;
	}

	/**
	 * The internal identifier of this account. Unique across all accounts. When
	 * possible, kept internal and not shared with members.
	 */
	public Long getId() {
		return id;
	}

	/**
	 * The first name of the person associated with this account.
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * The last name, or surname, of the person associated with this account.
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * Convenience operation that derives the full name of the account holder by
	 * combining his or her first and last names.
	 */
	public String getFullName() {
		return firstName + " " + lastName;
	}

	/**
	 * The email address on file for this account. May be unique across all
	 * accounts. If so, may be used as a credential in user authentication
	 * scenarios.
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * The username on file for this account. Optional. When present, is unique
	 * across all accounts. If present, preferred as a credential in user
	 * authentication scenarios.
	 */
	public String getUsername() {
		return username;
	}

	public String toString() {
		return id.toString();
	}
}