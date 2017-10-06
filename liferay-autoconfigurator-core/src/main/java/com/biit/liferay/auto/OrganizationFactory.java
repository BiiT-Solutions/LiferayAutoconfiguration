package com.biit.liferay.auto;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.biit.liferay.log.LiferayClientLogger;
import com.biit.utils.file.FileReader;
import com.liferay.portal.model.Organization;

public class OrganizationFactory extends JsonFactory<Organization> {
	private final static String RESOURCE_FOLDER = "organizations";
	private static OrganizationFactory instance;

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
		List<File> definitions = getDefinitions();
		List<Organization> organizations = new ArrayList<>();
		for (File file : definitions) {
			try {
				String fileContent = FileReader.readFile(file);
				Organization user = decodeFromJson(fileContent, Organization.class);
				organizations.add(user);
			} catch (IOException e) {
				LiferayClientLogger.errorMessage(this.getClass().getName(), e);
			}
		}
		return organizations;
	}

}
