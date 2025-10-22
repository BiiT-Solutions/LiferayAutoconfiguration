package com.biit.liferay.auto.configuration;

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
import com.biit.utils.configuration.ConfigurationReader;
import com.biit.utils.configuration.PropertiesSourceFile;
import com.biit.utils.configuration.SystemVariablePropertiesSourceFile;
import com.biit.utils.configuration.exceptions.PropertyNotFoundException;

public final class AutoConfigurationReader extends ConfigurationReader {
    private static final String CONFIG_FILE = "settings.conf";
    private static final String SYSTEM_VARIABLE_CONFIG = "LIFERAY_AUTOCONFIGURATOR_CONFIG";
    private static AutoConfigurationReader instance;

    // Tags
    private static final String ID_CONTENT_FOLDER = "content.folder";

    // Default values
    private static final String DEFAULT_CONTENT_FOLDER = System.getProperty("java.io.tmpdir");

    private AutoConfigurationReader() {
        super();

        addProperty(ID_CONTENT_FOLDER, DEFAULT_CONTENT_FOLDER);

        PropertiesSourceFile sourceFile = new PropertiesSourceFile(CONFIG_FILE);
        addPropertiesSource(sourceFile);

        SystemVariablePropertiesSourceFile systemSourceFile = new SystemVariablePropertiesSourceFile(SYSTEM_VARIABLE_CONFIG, CONFIG_FILE);
        addPropertiesSource(systemSourceFile);

        readConfigurations();
    }

    public static AutoConfigurationReader getInstance() {
        if (instance == null) {
            synchronized (AutoConfigurationReader.class) {
                if (instance == null) {
                    instance = new AutoConfigurationReader();
                }
            }
        }
        return instance;
    }

    private String getPropertyLogException(String propertyId) {
        try {
            return getProperty(propertyId);
        } catch (PropertyNotFoundException e) {
            LiferayAutoconfiguratorLogger.errorMessage(this.getClass().getName(), e);
            return null;
        }
    }

    @SuppressWarnings("unused")
    private String[] getPropertyCommaSeparatedValuesLogException(String propertyId) {
        try {
            return getCommaSeparatedValues(propertyId);
        } catch (PropertyNotFoundException e) {
            LiferayAutoconfiguratorLogger.errorMessage(this.getClass().getName(), e);
            return null;
        }
    }

    public String getContentFolder() {
        return getPropertyLogException(ID_CONTENT_FOLDER);
    }
}
