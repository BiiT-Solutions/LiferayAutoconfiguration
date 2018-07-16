package com.biit.liferay.auto.factories;

import java.io.File;
import java.util.List;

import com.biit.liferay.auto.configuration.AutoConfigurationReader;
import com.biit.liferay.auto.log.LiferayAutoconfiguratorLogger;
import com.biit.utils.file.FileReader;

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
