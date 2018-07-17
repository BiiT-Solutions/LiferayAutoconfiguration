package com.biit.liferay.auto.factories;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.biit.liferay.auto.log.LiferayAutoconfiguratorLogger;
import com.biit.liferay.model.KbArticle;
import com.biit.utils.file.FileReader;

public class ArticleFactory extends JsonFactory<KbArticle> {
	private final static String RESOURCE_FOLDER = "articles";
	private static ArticleFactory instance;
	private List<KbArticle> articles;

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
		if (articles == null) {
			articles = getElements(getDefinitionsFolderPath());
		}
		return articles;
	}

	private List<KbArticle> getElements(String path) {
		List<File> definitions = getDefinitions(path);
		List<KbArticle> articles = new ArrayList<>();
		for (File file : definitions) {
			if (file.isDirectory()) {
				articles.addAll(getElements(file.getAbsolutePath()));
			} else {
				try {
					String fileContent = FileReader.readFile(file);
					KbArticle article = decodeFromJson(fileContent, KbArticle.class);
					articles.add(article);
				} catch (IOException e) {
					LiferayAutoconfiguratorLogger.error(this.getClass().getName(), "Error decoding file '" + file
							+ "'. Check if it is a json file and is correctly formed.");
					LiferayAutoconfiguratorLogger.errorMessage(this.getClass().getName(), e);
				}
			}
		}
		return articles;
	}
}
