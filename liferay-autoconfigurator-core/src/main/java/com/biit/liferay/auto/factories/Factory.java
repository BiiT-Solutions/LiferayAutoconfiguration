package com.biit.liferay.auto.factories;

import java.io.File;
import java.util.List;

import com.biit.utils.file.FileReader;

public abstract class Factory<Type> {

	protected List<File> getDefinitions() {
		return FileReader.getResources(getResourceFolder());
	}

	protected abstract String getResourceFolder();

	public abstract List<Type> getElements();

}
