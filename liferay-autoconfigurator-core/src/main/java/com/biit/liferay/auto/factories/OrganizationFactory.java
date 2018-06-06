package com.biit.liferay.auto.factories;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.biit.liferay.auto.log.LiferayAutoconfiguratorLogger;
import com.biit.liferay.auto.model.ExtendedOrganization;
import com.biit.utils.file.FileReader;

public class OrganizationFactory extends JsonFactory<ExtendedOrganization> {
	private final static String RESOURCE_FOLDER = "organizations";
	private static OrganizationFactory instance;
	private List<ExtendedOrganization> organizations;

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
	public List<ExtendedOrganization> getElements() {
		if (organizations == null) {
			List<File> definitions = getDefinitions();
			organizations = new ArrayList<>();
			for (File file : definitions) {
				try {
					String fileContent = FileReader.readFile(file);
					ExtendedOrganization organization = decodeFromJson(fileContent, ExtendedOrganization.class);
					organizations.add(organization);
				} catch (IOException e) {
					LiferayAutoconfiguratorLogger.error(this.getClass().getName(), "Error decoding file '" + file
							+ "'. Check if it is a json file and is correctly formed.");
					LiferayAutoconfiguratorLogger.errorMessage(this.getClass().getName(), e);
				}
			}
		}
		// First organizations without parents.
		Collections.sort(organizations);
		return organizations;
	}

}
