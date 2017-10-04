package com.biit.liferay.auto;

import java.io.IOException;

import com.biit.liferay.access.ServiceAccess;
import com.biit.liferay.log.LiferayClientLogger;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonFactory<Type> {

	public Type decodeFromJson(String json, Class<Type> objectClass) throws JsonParseException, JsonMappingException, IOException {
		LiferayClientLogger.debug(ServiceAccess.class.getName(), "Decoding JSON object: " + json);
		ObjectMapper jsonMapper = new ObjectMapper();
		jsonMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		Type object = new ObjectMapper().readValue(json, objectClass);
		return object;

	}
}
