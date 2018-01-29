package com.biit.liferay.auto.factories;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.biit.liferay.auto.configuration.AutoConfigurationReader;
import com.liferay.portal.model.User;

@Test(groups = "userFactory")
public class UserFactoryTests {
	private final int USERS_IN_RESOURCES = 1;
	private final int USERS_IN_FOLDERS = USERS_IN_RESOURCES;

	private final String USERS_FOLDER_PATH = AutoConfigurationReader.getInstance().getContentFolder() + File.separator
			+ UserFactory.getInstance().getResourceFolder();

	private List<User> users;

	@Test
	public void getUsersFromResources() {
		Assert.assertEquals(UserFactory.getInstance().getDefinitions().size(), USERS_IN_RESOURCES);
		users = UserFactory.getInstance().getElements();
		Assert.assertEquals(users.size(), USERS_IN_RESOURCES);
	}

	@Test(dependsOnMethods = { "getUsersFromResources" })
	public void getUsersFromFolder() throws FileNotFoundException {
		File newFolder = new File(USERS_FOLDER_PATH);
		newFolder.mkdirs();
		newFolder.deleteOnExit();
		// Copy existing user data to a folder.
		int i = 0;
		for (User user : users) {
			String jsonCode = UserFactory.getInstance().encodeToJson(user);
			File file = new File(USERS_FOLDER_PATH + File.separator + "user" + i + ".json");
			file.deleteOnExit();
			try (PrintWriter out = new PrintWriter(USERS_FOLDER_PATH + File.separator + "user" + i + ".json")) {
				out.println(jsonCode);
			}
			i++;
		}

		Assert.assertEquals(USERS_FOLDER_PATH, UserFactory.getInstance().getDefinitionsFolderPath());
		Assert.assertEquals(UserFactory.getInstance().getDefinitions().size(), USERS_IN_FOLDERS);
		users = UserFactory.getInstance().getElements();
		Assert.assertEquals(users.size(), USERS_IN_FOLDERS);
	}

}
