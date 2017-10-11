package com.biit.liferay.auto.factories;

import java.io.IOException;
import java.util.Set;

import com.biit.liferay.access.ServiceAccess;
import com.biit.liferay.log.LiferayClientLogger;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

abstract class JsonFactory<Type> extends Factory<Type> {

	public Type decodeFromJson(String json, Class<Type> objectClass) throws JsonParseException, JsonMappingException, IOException {
		LiferayClientLogger.debug(ServiceAccess.class.getName(), "Decoding JSON object: " + json);
		ObjectMapper jsonMapper = new ObjectMapper();
		jsonMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		Type object = new ObjectMapper().readValue(json, objectClass);
		return object;

	}

	public Set<Type> decodeListFromJson(String json, Class<Type> objectClass) throws JsonParseException, JsonMappingException, IOException {
		Set<Type> myObjects = new ObjectMapper().readValue(json, new TypeReference<Set<Type>>() {
		});

		return myObjects;
	}

}
