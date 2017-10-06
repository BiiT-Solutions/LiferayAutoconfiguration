package com.biit.liferay.auto.model;

public class RoleSelection {
	private String role;
	private String organization;

	public String getRole() {
		return role != null ? role.trim() : null;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getOrganization() {
		return organization != null ? organization.trim() : null;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	@Override
	public String toString() {
		return role + ((organization != null) ? " (" + organization + ")" : "");
	}

}
