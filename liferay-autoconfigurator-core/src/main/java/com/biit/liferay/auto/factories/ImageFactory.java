package com.biit.liferay.auto.factories;

import java.io.File;
import java.util.List;

public class ImageFactory extends Factory<File> {
	private final static String RESOURCE_FOLDER = "images";
	private static ImageFactory instance;

	private static void createInstance() {
		if (instance == null) {
			synchronized (ImageFactory.class) {
				if (instance == null) {
					instance = new ImageFactory();
				}
			}
		}
	}

	public static ImageFactory getInstance() {
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
	public List<File> getElements() {
		return getDefinitions();
	}

}
