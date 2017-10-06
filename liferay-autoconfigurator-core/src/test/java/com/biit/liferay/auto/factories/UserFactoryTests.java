package com.biit.liferay.auto.factories;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.biit.liferay.auto.factories.UserFactory;
import com.liferay.portal.model.User;

@Test(groups = "userFactory")
public class UserFactoryTests {
	private final int USERS_IN_FOLDER = 1;

	private List<User> users;

	@Test
	public void getUsersFromResources() {
		Assert.assertEquals(UserFactory.getInstance().getDefinitions().size(), USERS_IN_FOLDER);
		users = UserFactory.getInstance().getElements();
		Assert.assertEquals(users.size(), USERS_IN_FOLDER);
	}

}
