package com.biit.liferay.auto.factories;

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
	private List<User> users;

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

	@Override
	protected String getResourceFolder() {
		return RESOURCE_FOLDER;
	}

	@Override
	public List<User> getElements() {
		if (users == null) {
			List<File> definitions = getDefinitions();
			users = new ArrayList<>();
			for (File file : definitions) {
				try {
					String fileContent = FileReader.readFile(file);
					User user = decodeFromJson(fileContent, User.class);
					users.add(user);
				} catch (IOException e) {
					LiferayClientLogger.error(this.getClass().getName(), "Error decoding file '" + file
							+ "'. Check if it is a json file and is correctly formed.");
					LiferayClientLogger.errorMessage(this.getClass().getName(), e);
				}
			}
		}
		return users;
	}

}
