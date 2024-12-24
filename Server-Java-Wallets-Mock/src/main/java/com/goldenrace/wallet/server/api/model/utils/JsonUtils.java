package com.goldenrace.wallet.server.api.model.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import java.io.IOException;

public class JsonUtils {

    private ObjectMapper mapper;

    public JsonUtils() {
        this.mapper = new ObjectMapper()
                .registerModule(new JodaModule())
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public JsonNode readTree(String json) throws IOException {
        return mapper.readTree(json);
    }

    public ObjectNode createObjectNode() {
        return mapper.createObjectNode();
    }

    public ArrayNode createArrayNode() {
        return mapper.createArrayNode();
    }

    public <T> T convertValue(Object fromValue, Class<T> toValueType) {
        return mapper.convertValue(fromValue, toValueType);
    }

    public <T> T convertValue(Object fromValue, TypeReference<T> toValueTypeRef) {
        return mapper.convertValue(fromValue, toValueTypeRef);
    }

    public <T> T unserialize(String json, Class<T> target) throws IOException {
        return mapper.readValue(json, target);
    }

    public <T> T unserialize(String json, TypeReference<T> typeReference) throws IOException {
        return mapper.readValue(json, typeReference);
    }

    public String serialize(Object origin) throws JsonProcessingException {
        return mapper.writeValueAsString(origin);
    }

}
