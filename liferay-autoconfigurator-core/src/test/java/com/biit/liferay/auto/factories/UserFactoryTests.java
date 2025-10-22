package com.biit.liferay.auto.factories;

/*-
 * #%L
 * Liferay Basic Configuration Creation (Core)
 * %%
 * Copyright (C) 2017 - 2025 BiiT Sourcing Solutions S.L.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

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
