package com.biit.liferay.auto.factories;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.biit.liferay.model.KbArticle;

@Test(groups = "articleFactory")
public class ArticleFactoryTests {
	private final int ARTICLES_IN_FOLDER = 1;

	private List<KbArticle> articles;

	@Test
	public void getArticlesFromResources() {
		Assert.assertEquals(ArticleFactory.getInstance().getDefinitions().size(), ARTICLES_IN_FOLDER);
		articles = ArticleFactory.getInstance().getElements();
		Assert.assertEquals(articles.size(), ARTICLES_IN_FOLDER);
	}

}
