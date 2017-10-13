package com.biit.liferay.auto.factories;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.biit.liferay.log.LiferayClientLogger;
import com.biit.liferay.model.KbArticle;
import com.biit.utils.file.FileReader;

public class ArticleFactory extends JsonFactory<KbArticle> {
	private final static String RESOURCE_FOLDER = "articles";
	private static ArticleFactory instance;

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
		List<File> definitions = getDefinitions();
		List<KbArticle> articles = new ArrayList<>();
		for (File file : definitions) {
			try {
				String fileContent = FileReader.readFile(file);
				KbArticle article = decodeFromJson(fileContent, KbArticle.class);
				articles.add(article);
			} catch (IOException e) {
				LiferayClientLogger.errorMessage(this.getClass().getName(), e);
			}
		}
		return articles;
	}

}
