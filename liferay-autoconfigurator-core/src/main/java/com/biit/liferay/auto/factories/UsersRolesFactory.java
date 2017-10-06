package com.biit.liferay.auto.factories;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.biit.liferay.auto.model.UserRole;
import com.biit.liferay.log.LiferayClientLogger;
import com.biit.utils.file.FileReader;

public class UsersRolesFactory extends JsonFactory<UserRole> {
	private final static String RESOURCE_FOLDER = "usersRoles";
	private static UsersRolesFactory instance;

	private static void createInstance() {
		if (instance == null) {
			synchronized (UsersRolesFactory.class) {
				if (instance == null) {
					instance = new UsersRolesFactory();
				}
			}
		}
	}

	public static UsersRolesFactory getInstance() {
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
	public List<UserRole> getElements() {
		List<File> definitions = getDefinitions();
		List<UserRole> usersroles = new ArrayList<>();
		for (File file : definitions) {
			try {
				String fileContent = FileReader.readFile(file);
				UserRole role = decodeFromJson(fileContent, UserRole.class);
				usersroles.add(role);
			} catch (IOException e) {
				LiferayClientLogger.errorMessage(this.getClass().getName(), e);
			}
		}
		return usersroles;
	}

}