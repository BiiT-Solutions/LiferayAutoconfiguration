package com.biit.liferay.auto.factories;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.biit.liferay.log.LiferayClientLogger;
import com.biit.utils.file.FileReader;
import com.liferay.portal.model.Role;

public class RoleFactory extends JsonFactory<Role> {
	private final static String RESOURCE_FOLDER = "roles";
	private static RoleFactory instance;

	private static void createInstance() {
		if (instance == null) {
			synchronized (RoleFactory.class) {
				if (instance == null) {
					instance = new RoleFactory();
				}
			}
		}
	}

	public static RoleFactory getInstance() {
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
	public List<Role> getElements() {
		List<File> definitions = getDefinitions();
		List<Role> roles = new ArrayList<>();
		for (File file : definitions) {
			try {
				String fileContent = FileReader.readFile(file);
				Role role = decodeFromJson(fileContent, Role.class);
				roles.add(role);
			} catch (IOException e) {
				LiferayClientLogger.errorMessage(this.getClass().getName(), e);
			}
		}
		return roles;
	}

}
