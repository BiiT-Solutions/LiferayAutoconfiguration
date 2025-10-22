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

import com.liferay.portal.model.Organization;

public class ExtendedOrganization extends Organization implements Comparable<ExtendedOrganization> {
    private static final long serialVersionUID = 2246642955405679365L;

    private String parentOrganizationName;

    public String getParentOrganizationName() {
        return parentOrganizationName;
    }

    public void setParentOrganizationName(String parentOrganizationName) {
        this.parentOrganizationName = parentOrganizationName;
    }

    @Override
    public int compareTo(ExtendedOrganization organization) {
        if (this.getParentOrganizationName() == null) {
            if (organization.getParentOrganizationName() == null) {
                return this.getName().compareTo(organization.getName());
            } else {
                return 1;
            }
        } else {
            if (organization.getParentOrganizationName() == null) {
                return -1;
            } else {
                this.getName().compareTo(organization.getName());
            }
        }
        return 0;
    }

}
