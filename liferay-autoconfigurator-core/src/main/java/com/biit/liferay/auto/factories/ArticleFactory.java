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
import com.biit.liferay.model.KbArticle;
import com.biit.utils.file.FileReader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ArticleFactory extends JsonFactory<KbArticle> {
    private static final String RESOURCE_FOLDER = "articles";
    private static ArticleFactory instance;
    private Map<String, List<KbArticle>> filesByFolder;

    private static void createInstance() {
        if (instance == null) {
            synchronized (ArticleFactory.class) {
                if (instance == null) {
                    instance = new ArticleFactory();
                }
            }
        }
    }

    public static ArticleFactory getInstance() {
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
    public List<KbArticle> getElements() {
        if (filesByFolder == null) {
            filesByFolder = new HashMap<>();
            getElements(filesByFolder, null, getDefinitionsFolderPath());
        }

        List<KbArticle> totalArticles = new ArrayList<>();
        for (Entry<String, List<KbArticle>> entry : filesByFolder.entrySet()) {
            totalArticles.addAll(entry.getValue());
        }
        return totalArticles;
    }

    private void getElements(Map<String, List<KbArticle>> filesByFolder, String folder, String path) {
        List<File> definitions = getDefinitions(path);
        if (filesByFolder.get(folder) == null) {
            filesByFolder.put(folder, new ArrayList<KbArticle>());
        }
        for (File file : definitions) {
            if (file.isDirectory()) {
                getElements(filesByFolder, file.getName(), file.getAbsolutePath());
            } else {
                try {
                    String fileContent = FileReader.readFile(file);
                    KbArticle article = decodeFromJson(fileContent, KbArticle.class);
                    filesByFolder.get(folder).add(article);
                } catch (IOException e) {
                    LiferayAutoconfiguratorLogger.error(this.getClass().getName(),
                            "Error decoding file '" + file + "'. Check if it is a json file and is correctly formed.");
                    LiferayAutoconfiguratorLogger.errorMessage(this.getClass().getName(), e);
                }
            }
        }
    }

    public Map<String, List<KbArticle>> getFilesByFolder() {
        if (filesByFolder == null) {
            filesByFolder = new HashMap<>();
            getElements(filesByFolder, null, getDefinitionsFolderPath());
        }
        return filesByFolder;
    }
}
