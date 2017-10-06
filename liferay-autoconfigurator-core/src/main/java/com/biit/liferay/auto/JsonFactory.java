package com.biit.liferay.auto;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.biit.liferay.access.ServiceAccess;
import com.biit.liferay.log.LiferayClientLogger;
import com.biit.utils.file.FileReader;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

abstract class JsonFactory<Type> {

	public Type decodeFromJson(String json, Class<Type> objectClass) throws JsonParseException, JsonMappingException, IOException {
		LiferayClientLogger.debug(ServiceAccess.class.getName(), "Decoding JSON object: " + json);
		ObjectMapper jsonMapper = new ObjectMapper();
		jsonMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		Type object = new ObjectMapper().readValue(json, objectClass);
		return object;

	}

	protected List<File> getDefinitions() {
		return FileReader.getResources(getResourceFolder());
	}

	protected abstract String getResourceFolder();

	public abstract List<Type> getElements();
}
