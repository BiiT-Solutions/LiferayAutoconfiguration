package com.biit.liferay.auto.model;

import java.util.List;

import com.liferay.portal.model.Role;

public class ExtendedRole extends Role {
	private static final long serialVersionUID = -4214805612988310843L;

	private List<String> activities;
	private String classification;
	private String group;
	private String translation;

	public List<String> getActivities() {
		return activities;
	}

	public void setActivities(List<String> activities) {
		this.activities = activities;
	}

	public String getClassification() {
		return classification;
	}

	public void setClassification(String classification) {
		this.classification = classification;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getTranslation() {
		return translation;
	}

	public void setTranslation(String translation) {
		this.translation = translation;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
