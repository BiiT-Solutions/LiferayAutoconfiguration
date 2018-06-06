package com.biit.liferay.auto.model;

import com.liferay.portal.model.Organization;

public class ExtendedOrganization extends Organization implements Comparable<ExtendedOrganization> {
	private static final long serialVersionUID = 2246642955405679365L;

	private String parentOrganizationName;

	public String getParentOrganizationName() {
		return parentOrganizationName;
	}

	public void setParentOrganizationName(String parentOrganizationName) {
		this.parentOrganizationName = parentOrganizationName;
	}

	@Override
	public int compareTo(ExtendedOrganization organization) {
		if (this.getParentOrganizationName() == null) {
			if (organization.getParentOrganizationName() == null) {
				return this.getName().compareTo(organization.getName());
			} else {
				return 1;
			}
		} else {
			if (organization.getParentOrganizationName() == null) {
				return -1;
			} else {
				this.getName().compareTo(organization.getName());
			}
		}
		return 0;
	}

}
