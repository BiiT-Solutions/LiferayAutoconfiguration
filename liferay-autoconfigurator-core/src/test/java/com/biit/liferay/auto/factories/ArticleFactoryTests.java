package com.biit.liferay.auto.factories;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.biit.liferay.auto.Main;
import com.biit.liferay.model.KbArticle;

@Test(groups = "articleFactory")
public class ArticleFactoryTests {
	private static final int ARTICLES_IN_FOLDER = 3;

	private List<KbArticle> articles;

	@Test
	public void getArticlesFromResources() {
		Assert.assertEquals(ArticleFactory.getInstance().getDefinitions().size(), ARTICLES_IN_FOLDER);
		articles = ArticleFactory.getInstance().getElements();
		Assert.assertEquals(articles.size(), ARTICLES_IN_FOLDER);
	}

	@Test
	public void obtainUrlTitle() {
		Assert.assertEquals(Main.getUrlString("BMI"), "bmi");
		Assert.assertEquals(Main.getUrlString("LEC BRAVOS"), "lec-bravos");
		Assert.assertEquals(Main.getUrlString("LEC Par-q"), "lec-par-q");
	}

}
