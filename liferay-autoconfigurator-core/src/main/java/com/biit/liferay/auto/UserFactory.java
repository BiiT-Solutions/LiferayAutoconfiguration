package com.biit.liferay.auto;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.biit.liferay.log.LiferayClientLogger;
import com.biit.utils.file.FileReader;
import com.liferay.portal.model.User;

public class UserFactory extends JsonFactory<User> {
	private final static String RESOURCE_FOLDER = "users";
	private static UserFactory instance;

	private static void createInstance() {
		if (instance == null) {
			synchronized (UserFactory.class) {
				if (instance == null) {
					instance = new UserFactory();
				}
			}
		}
	}

	public static UserFactory getInstance() {
		if (instance == null) {
			createInstance();
		}
		return instance;
	}

	protected List<File> getUsersDefinition() {
		return FileReader.getResources(RESOURCE_FOLDER);
	}

	public List<User> getUsers() {
		List<File> usersDefinitions = getUsersDefinition();
		List<User> users = new ArrayList<>();
		for (File file : usersDefinitions) {
			try {
				String userContent = FileReader.readFile(file);
				User user = decodeFromJson(userContent, User.class);
				users.add(user);
			} catch (IOException e) {
				LiferayClientLogger.errorMessage(this.getClass().getName(), e);
			}
		}
		return users;
	}

}
