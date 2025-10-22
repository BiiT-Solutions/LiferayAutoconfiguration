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
		for (ExtendedRole role : roles) {
			if (role.getName().equals("base-form-drools_web-service-user")) {
				Assert.assertNull(role.getActivities());
				break;
			} else if (role.getName().equals("usmo_physiotherapist")) {
				Assert.assertEquals(role.getActivities().size(), 25);
				break;
			}
			// Not found. test failed.
			Assert.fail();
		}
	}

}
