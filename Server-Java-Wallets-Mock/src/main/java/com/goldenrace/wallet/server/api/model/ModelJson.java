package com.goldenrace.wallet.server.api.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.goldenrace.wallet.server.api.model.utils.JsonUtils;
import com.goldenrace.wallet.server.properties.logging.AppLog;
import com.goldenrace.wallet.server.properties.logging.AppLogger;
import com.goldenrace.wallet.server.properties.logging.IAppLogger;
import org.joda.time.DateTime;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ModelJson {

    private static final IAppLogger LOGGER = AppLogger.getLogger(ModelJson.class);

    private static final JsonUtils JSON_UTILS = new JsonUtils();

    private final ObjectNode objectNode;

    public ModelJson() {
        this.objectNode = JSON_UTILS.createObjectNode();
    }

    public ModelJson(JsonNode jsonNode) throws IOException {
        if (Objects.isNull(jsonNode)) {
            throw new IOException("Invalid empty JsonNode");
        }
        checkObjectNode(jsonNode);
        this.objectNode = (ObjectNode) jsonNode;
    }

    private <T extends ModelJson> T createJson(String logId, Class<T> jsonClass, JsonNode jsonNode) {
        try {
            return jsonClass.getConstructor(JsonNode.class).newInstance(jsonNode);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            logInstanceObjectNodeJsonError(logId, jsonClass, jsonNode);
            return null;
        }
    }

    /**
     * Check if {@link JsonNode} is not a {@link MissingNode}, not contains a null value, and is an object
     *
     * @param node
     * @return
     */
    private boolean isValidObjectNode(JsonNode node) {
        return isValidNode(node) && node.isObject();
    }

    private boolean isValidNumberNode(JsonNode node) {
        return isValidNode(node) && node.isNumber();
    }

    private void checkObjectNode(JsonNode jsonNode) throws IOException {
        if (!isValidObjectNode(jsonNode)) {
            throw new IOException(String.format("Invalid JsonNode type: %s", jsonNode.getNodeType().toString()));
        }
    }

    private void validateJson(String json) throws IOException {
        if (Objects.isNull(json) || json.isEmpty() || json.codePoints().allMatch(Character::isWhitespace)) {
            throw new IOException("Invalid empty json");
        }
    }

    private <T> T getValue(Object fromValue, Class<T> toValueType) {
        try {
            return JSON_UTILS.convertValue(fromValue, toValueType);
        } catch (IllegalArgumentException ex) {
            LOGGER.error(AppLog.Builder.id("getValue")
                                 .message("Conversion fails due to incompatible type: from={}, to={}", fromValue, toValueType));
            return null;
        }
    }

    private Iterator<JsonNode> getListIterator(JsonNode node) {
        if (Objects.nonNull(node) && node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            if (arrayNode.size() > 0) {
                return arrayNode.elements();
            } else {
                return Collections.emptyIterator();
            }
        } else {
            return Collections.emptyIterator();
        }
    }

    private <T extends ModelJson> List<T> getJsons(String logId, Iterator<JsonNode> iterator, Class<T> jsonClass) {
        List<T> list = new ArrayList<>();
        while (iterator.hasNext()) {
            T json = createJson(logId, jsonClass, iterator.next());
            if (Objects.nonNull(json)) {
                list.add(json);
            }
        }
        return list;
    }

    private void setAll(String fieldName, Map<String, JsonNode> map) {
        JsonNode jsonNode = this.objectNode.get(fieldName);
        if (!isValidNode(jsonNode)) {
            jsonNode = JSON_UTILS.createObjectNode();
            this.objectNode.set(fieldName, jsonNode);
        }
        ((ObjectNode) jsonNode).setAll(map);
    }

    protected ObjectNode getObjectNode() {
        return objectNode;
    }

    protected <T extends ModelJson> void logInstanceObjectNodeJsonError(String logId, Class<T> jsonClass, JsonNode jsonNode) {
        LOGGER.warn(AppLog.Builder.id(logId)
                            .message("Invalid JsonNode to instantiate {}: {}", jsonClass.getSimpleName(), jsonNode));
    }

    public JsonNode getJsonNode() {
        return this.objectNode.deepCopy();
    }

    public boolean hasFieldName(String fieldName) {
        return this.objectNode.has(fieldName);
    }

    /**
     * Check if {@link JsonNode} is not a {@link MissingNode}, not contains a null value
     *
     * @param node
     * @return
     */
    public boolean isValidNode(JsonNode node) {
        return Objects.nonNull(node) && !node.isMissingNode() && !node.isNull();
    }

    public JsonNode getJsonNode(String fieldName) {
        return this.objectNode.get(fieldName);
    }

    public JsonNode getJsonNode(String... fieldNames) {
        if (Objects.isNull(fieldNames) || fieldNames.length == 0) {
            return MissingNode.getInstance();
        }
        JsonNode jsonNode = this.objectNode.path(fieldNames[0]);
        for (int i = 1; i < fieldNames.length; i++) {
            jsonNode = jsonNode.path(fieldNames[i]);
        }
        return jsonNode;
    }

    public ArrayNode getOrCreateArrayNode(String fieldName) {
        JsonNode node = this.objectNode.get(fieldName);
        if (!isValidNode(node)) {
            ArrayNode arrayNode = JSON_UTILS.createArrayNode();
            this.objectNode.set(fieldName, arrayNode);
            return arrayNode;
        } else if (node.isArray()) {
            return ((ArrayNode) node);
        } else {
            LOGGER.warn(AppLog.Builder.id("")
                                .message("Invalid \"{}\" ArrayNode type: %s", fieldName, node.getNodeType()));
            return null;
        }
    }

    public <T extends ModelJson> T getJson(String logId, String fieldName, Class<T> jsonClass) {
        return createJson(logId, jsonClass, getJsonNode(fieldName));
    }

    public <T extends ModelJson> void setJson(String fieldName, T json) {
        this.objectNode.set(fieldName, json.getObjectNode());
    }

    public <T extends ModelJson> List<T> getJsons(String logId, String fieldName, Class<T> jsonClass) {
        Iterator<JsonNode> iterator = getListIterator(fieldName);
        return getJsons(logId, iterator, jsonClass);
    }

    public <T extends ModelJson> List<T> getJsons(String logId, JsonNode node, Class<T> jsonClass) {
        Iterator<JsonNode> iterator = getListIterator(node);
        return getJsons(logId, iterator, jsonClass);
    }

    public <T extends ModelJson> void setAllJsons(String fieldName, Map<String, T> jsons) {
        if (Objects.nonNull(jsons) && !jsons.isEmpty()) {
            Map<String, JsonNode> map = new HashMap<>();
            jsons.forEach((key, json) -> map.put(key, json.getObjectNode()));
            setAll(fieldName, map);
        }
    }

    public <T extends ModelJson> void setAllJsonLists(String fieldName, Map<String, List<T>> jsonLists) {
        if (Objects.nonNull(jsonLists) && !jsonLists.isEmpty()) {
            Map<String, JsonNode> map = new HashMap<>();
            jsonLists.forEach((key, list) -> {
                ArrayNode arrayNode = JSON_UTILS.createArrayNode();
                list.forEach(json -> arrayNode.add(json.getObjectNode()));
                map.put(key, arrayNode);
            });
            setAll(fieldName, map);
        }
    }

    public void setAllStrings(String fieldName, Map<String, String> jsons) {
        if (Objects.nonNull(jsons) && !jsons.isEmpty()) {
            Map<String, JsonNode> map = new HashMap<>();
            jsons.forEach((key, value) -> map.put(key, new TextNode(value)));
            setAll(fieldName, map);
        }
    }

    public String getString(String fieldName, String defaultValue) {
        JsonNode node = this.objectNode.get(fieldName);
        if (Objects.nonNull(node) && node.isTextual()) {
            return node.asText();
        } else {
            return defaultValue;
        }
    }

    public String getString(String fieldName) {
        return getString(fieldName, null);
    }

    public void putString(String fieldName, String value) {
        this.objectNode.put(fieldName, value);
    }

    public Integer getInteger(String fieldName) {
        JsonNode node = this.objectNode.get(fieldName);
        if (Objects.nonNull(node) && isValidNumberNode(node)) {
            return node.asInt();
        } else {
            return null;
        }
    }

    public void putInteger(String fieldName, Integer value) {
        this.objectNode.put(fieldName, value);
    }

    public Long getLong(String fieldName) {
        JsonNode node = this.objectNode.get(fieldName);
        if (Objects.nonNull(node) && isValidNumberNode(node)) {
            return node.asLong();
        } else {
            return null;
        }
    }

    public void putLong(String fieldName, Long value) {
        this.objectNode.put(fieldName, value);
    }

    public Double getDouble(String fieldName, Double defaultValue) {
        JsonNode node = this.objectNode.get(fieldName);
        if (Objects.nonNull(node) && isValidNumberNode(node)) {
            return node.asDouble();
        } else {
            return defaultValue;
        }
    }

    public Double getDouble(String fieldName) {
        return getDouble(fieldName, null);
    }

    public void putDouble(String fieldName, Double value) {
        this.objectNode.put(fieldName, value);
    }

    public BigDecimal getBigDecimal(String fieldName) {
        JsonNode node = this.objectNode.get(fieldName);
        if (Objects.nonNull(node) && isValidNumberNode(node)) {
            return getValue(node.asText(), BigDecimal.class);
        } else {
            return null;
        }
    }

    public void putBigDecimal(String fieldName, BigDecimal value) {
        this.objectNode.put(fieldName, value);
    }

    public DateTime getDateTime(String fieldName) {
        JsonNode node = this.objectNode.get(fieldName);
        if (isValidNode(node)) {
            return getValue(node.asText(), DateTime.class);
        } else {
            return null;
        }
    }

    public void putDateTime(String fieldName, DateTime value) {
        this.objectNode.put(fieldName, JSON_UTILS.convertValue(value, String.class));
    }

    public Iterator<JsonNode> getListIterator(String fieldName) {
        return getListIterator(this.objectNode.get(fieldName));
    }

    public <T extends ModelJson> void addList(String fieldName, List<T> values) {
        ArrayNode arrayNode = getOrCreateArrayNode(fieldName);
        if (Objects.nonNull(arrayNode)) {
            values.forEach(value -> arrayNode.add(value.getObjectNode()));
        }
    }

    public <T extends ModelJson> void putList(String fieldName, List<T> values) {
        ArrayNode arrayNode = getOrCreateArrayNode(fieldName);
        if (Objects.nonNull(arrayNode)) {
            arrayNode.removeAll();
            values.forEach(value -> arrayNode.add(value.getObjectNode()));
        }
    }

}
