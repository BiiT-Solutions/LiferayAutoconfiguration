package com.biit.liferay.auto.factories;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.biit.liferay.auto.log.LiferayAutoconfiguratorLogger;
import com.biit.liferay.log.LiferayClientLogger;
import com.biit.utils.file.FileReader;
import com.liferay.portal.model.Organization;

public class OrganizationFactory extends JsonFactory<Organization> {
	private final static String RESOURCE_FOLDER = "organizations";
	private static OrganizationFactory instance;
	private List<Organization> organizations;

	private static void createInstance() {
		if (instance == null) {
			synchronized (OrganizationFactory.class) {
				if (instance == null) {
					instance = new OrganizationFactory();
				}
			}
		}
	}

	public static OrganizationFactory getInstance() {
		if (instance == null) {
			createInstance();
		}
		return instance;
	}

	@Override
	protected String getResourceFolder() {
		return RESOURCE_FOLDER;
	}

	@Override
	public List<Organization> getElements() {
		if (organizations == null) {
			List<File> definitions = getDefinitions();
			organizations = new ArrayList<>();
			for (File file : definitions) {
				try {
					String fileContent = FileReader.readFile(file);
					Organization organization = decodeFromJson(fileContent, Organization.class);
					organizations.add(organization);
				} catch (IOException e) {
					LiferayClientLogger.error(this.getClass().getName(), "Error decoding file '" + file
							+ "'. Check if it is a json file and is correctly formed.");
					LiferayAutoconfiguratorLogger.errorMessage(this.getClass().getName(), e);
				}
			}
		}
		return organizations;
	}

}
