package com.biit.liferay.auto.factories;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.biit.liferay.auto.log.LiferayAutoconfiguratorLogger;
import com.biit.liferay.model.KbArticle;
import com.biit.utils.file.FileReader;

public class ArticleFactory extends JsonFactory<KbArticle> {
	private final static String RESOURCE_FOLDER = "articles";
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
