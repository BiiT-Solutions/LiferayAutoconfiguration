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

import com.biit.liferay.auto.log.LiferayAutoconfiguratorLogger;
import com.biit.liferay.auto.model.ExtendedRole;
import com.biit.usermanager.entity.IRole;
import com.biit.utils.file.FileReader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RoleFactory extends JsonFactory<ExtendedRole> {
    private static final String RESOURCE_FOLDER = "roles";
    private List<ExtendedRole> roles;
    private static RoleFactory instance;

    private static void createInstance() {
        if (instance == null) {
            synchronized (RoleFactory.class) {
                if (instance == null) {
                    instance = new RoleFactory();
                }
            }
        }
    }

    public static RoleFactory getInstance() {
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
    public List<ExtendedRole> getElements() {
        if (roles == null) {
            List<File> definitions = getDefinitions();
            roles = new ArrayList<>();
            for (File file : definitions) {
                try {
                    String fileContent = FileReader.readFile(file);
                    ExtendedRole role = decodeFromJson(fileContent, ExtendedRole.class);
                    roles.add(role);
                } catch (IOException e) {
                    LiferayAutoconfiguratorLogger.error(this.getClass().getName(), "Error decoding file '" + file
                            + "'. Check if it is a json file and is correctly formed.");
                    LiferayAutoconfiguratorLogger.errorMessage(this.getClass().getName(), e);
                }
            }
        }
        return roles;
    }

    public ExtendedRole getElement(IRole<Long> role) {
        for (ExtendedRole extendedRole : getElements()) {
            if (Objects.equals(extendedRole.getUniqueName(), role.getUniqueName())) {
                return extendedRole;
            }
        }
        return null;
    }

}
