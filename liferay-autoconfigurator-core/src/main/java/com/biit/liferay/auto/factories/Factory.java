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

import com.biit.liferay.auto.configuration.AutoConfigurationReader;
import com.biit.liferay.auto.log.LiferayAutoconfiguratorLogger;
import com.biit.utils.file.FileReader;

import java.io.File;
import java.util.List;

public abstract class Factory<Type> {

    protected List<File> getDefinitions() {
        return getDefinitions(getDefinitionsFolderPath());
    }

    protected List<File> getDefinitions(String resource) {
        // Absolute path.
        File folder = new File(resource);
        if (folder != null && folder.exists()) {
            LiferayAutoconfiguratorLogger.info(this.getClass().getName(), "Accessing to files in '" + folder.getAbsolutePath() + "'.");
            return FileReader.getFiles(folder);
        }
        // Resource path
        LiferayAutoconfiguratorLogger.debug(this.getClass().getName(), "Accessing to files in project resources.");
        return FileReader.getResources(getResourceFolder());
    }

    protected String getDefinitionsFolderPath() {
        File folder = new File(AutoConfigurationReader.getInstance().getContentFolder() + File.separator + getResourceFolder());
        if (folder.exists()) {
            return folder.getAbsolutePath();
        }
        return getResourceFolder();
    }

    protected abstract String getResourceFolder();

    public abstract List<Type> getElements();

}
