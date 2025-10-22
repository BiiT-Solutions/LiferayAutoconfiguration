package com.biit.liferay.auto.factories;

/*-
 * #%L
 * Liferay Basic Configuration Creation (Core)
 * %%
 * Copyright (C) 2017 - 2025 BiiT Sourcing Solutions S.L.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.io.IOException;
import java.util.Set;

import com.biit.liferay.access.ServiceAccess;
import com.biit.liferay.auto.log.LiferayAutoconfiguratorLogger;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

abstract class JsonFactory<Type> extends Factory<Type> {

    public Type decodeFromJson(String json, Class<Type> objectClass) throws JsonParseException, JsonMappingException, IOException {
        LiferayAutoconfiguratorLogger.debug(ServiceAccess.class.getName(), "Decoding JSON object: " + json);
        ObjectMapper jsonMapper = new ObjectMapper();
        jsonMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        Type object = new ObjectMapper().readValue(json, objectClass);
        return object;
    }

    public String encodeToJson(Type element) {
        ObjectMapper jsonMapper = new ObjectMapper();
        jsonMapper.setSerializationInclusion(Include.NON_NULL);
        try {
            return jsonMapper.writeValueAsString(element);
        } catch (JsonProcessingException e) {
            LiferayAutoconfiguratorLogger.errorMessage(this.getClass().getName(), e);
        }
        return null;
    }

    public Set<Type> decodeListFromJson(String json, Class<Type> objectClass) throws JsonParseException, JsonMappingException, IOException {
        Set<Type> myObjects = new ObjectMapper().readValue(json, new TypeReference<Set<Type>>() {
        });

        return myObjects;
    }

}
