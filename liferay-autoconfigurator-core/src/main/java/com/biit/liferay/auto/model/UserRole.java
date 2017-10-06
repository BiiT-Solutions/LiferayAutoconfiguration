package com.biit.liferay.auto.model;

import java.util.Set;

public class UserRole {
	private String user;

	private Set<RoleSelection> roles;

	public String getUser() {
		return user != null ? user.trim() : null;
	}

	public void setUser(String user) {
		this.user = user;
	}

	@Override
	public String toString() {
		return "{" + user + ": " + roles + "}";
	}

	public Set<RoleSelection> getRoles() {
		return roles;
	}

	public void setRoles(Set<RoleSelection> roles) {
		this.roles = roles;
	}
}
