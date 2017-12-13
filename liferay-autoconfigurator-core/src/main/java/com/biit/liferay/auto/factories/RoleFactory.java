package com.biit.liferay.auto.factories;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.biit.liferay.auto.model.ExtendedRole;
import com.biit.liferay.log.LiferayClientLogger;
import com.biit.utils.file.FileReader;

public class RoleFactory extends JsonFactory<ExtendedRole> {
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
	public List<ExtendedRole> getElements() {
		List<File> definitions = getDefinitions();
		List<ExtendedRole> roles = new ArrayList<>();
		for (File file : definitions) {
			try {
				String fileContent = FileReader.readFile(file);
				ExtendedRole role = decodeFromJson(fileContent, ExtendedRole.class);
				roles.add(role);
			} catch (IOException e) {
				LiferayClientLogger.errorMessage(this.getClass().getName(), e);
			}
		}
		return roles;
	}

}
