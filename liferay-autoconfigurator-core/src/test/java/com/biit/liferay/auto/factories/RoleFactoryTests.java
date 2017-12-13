package com.biit.liferay.auto.factories;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.biit.liferay.auto.model.ExtendedRole;

@Test(groups = "roleFactory")
public class RoleFactoryTests {
	private final int ROLES_IN_FOLDER = 2;

	private List<ExtendedRole> roles;

	@Test
	public void getUsersFromResources() {
		Assert.assertEquals(RoleFactory.getInstance().getDefinitions().size(), ROLES_IN_FOLDER);
		roles = RoleFactory.getInstance().getElements();
		Assert.assertEquals(roles.size(), ROLES_IN_FOLDER);
		Assert.assertNull(roles.get(0).getActivities());
		Assert.assertEquals(roles.get(1).getActivities().size(), 25);
	}

}
