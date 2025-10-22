package com.biit.liferay.auto.model;

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

import java.util.Set;

public class UserRole {
    private String user;

    private Set<RoleSelection> roles;

    public String getUser() {
        return user != null ? user.trim() : null;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "{" + user + ": " + roles + "}";
    }

    public Set<RoleSelection> getRoles() {
        return roles;
    }

    public void setRoles(Set<RoleSelection> roles) {
        this.roles = roles;
    }
}
