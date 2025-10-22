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
import com.biit.utils.file.FileReader;
import com.liferay.portal.model.User;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserFactory extends JsonFactory<User> {
    private static final String RESOURCE_FOLDER = "users";
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
                    LiferayAutoconfiguratorLogger.error(this.getClass().getName(), "Error decoding file '" + file
                            + "'. Check if it is a json file and is correctly formed.");
                    LiferayAutoconfiguratorLogger.errorMessage(this.getClass().getName(), e);
                }
            }
        }
        return users;
    }

}
