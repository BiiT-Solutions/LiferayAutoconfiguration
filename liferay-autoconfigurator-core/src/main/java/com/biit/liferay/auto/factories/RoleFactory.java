package com.biit.liferay.auto.factories;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.biit.liferay.auto.log.LiferayAutoconfiguratorLogger;
import com.biit.liferay.auto.model.ExtendedRole;
import com.biit.liferay.log.LiferayClientLogger;
import com.biit.usermanager.entity.IRole;
import com.biit.utils.file.FileReader;

public class RoleFactory extends JsonFactory<ExtendedRole> {
	private final static String RESOURCE_FOLDER = "roles";
	private List<ExtendedRole> roles;
	private static RoleFactory instance;

	private static void createInstance() {
		if (instance == null) {
			synchronized (RoleFactory.class) {
				if (instance == null) {
					instance = new RoleFactory();
				}
			}
		}
	}

	public static RoleFactory getInstance() {
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
	public List<ExtendedRole> getElements() {
		if (roles == null) {
			List<File> definitions = getDefinitions();
			roles = new ArrayList<>();
			for (File file : definitions) {
				try {
					String fileContent = FileReader.readFile(file);
					ExtendedRole role = decodeFromJson(fileContent, ExtendedRole.class);
					roles.add(role);
				} catch (IOException e) {
					LiferayClientLogger.error(this.getClass().getName(), "Error decoding file '" + file
							+ "'. Check if it is a json file and is correctly formed.");
					LiferayAutoconfiguratorLogger.errorMessage(this.getClass().getName(), e);
				}
			}
		}
		return roles;
	}

	public ExtendedRole getElement(IRole<Long> role) {
		for (ExtendedRole extendedRole : getElements()) {
			if (Objects.equals(extendedRole.getUniqueName(), role.getUniqueName())) {
				return extendedRole;
			}
		}
		return null;
	}

}
