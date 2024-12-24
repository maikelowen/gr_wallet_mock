package com.goldenrace.wallet.server.properties;

import com.goldenrace.wallet.server.properties.logging.AppLog;
import com.goldenrace.wallet.server.properties.logging.AppLogger;
import com.goldenrace.wallet.server.properties.logging.IAppLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionException;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import static org.apache.logging.log4j.util.Strings.isBlank;


@Component
public class Settings {

    private static final IAppLogger LOGGER = AppLogger.getLogger(Settings.class);

    @Autowired
    private Environment env;

    /**
     * Return the property value associated with the given key,
     * or {@code null} if the key cannot be resolved.
     *
     * @param key the property name to resolve
     */
    public String get(String key) {
        return env.getProperty(key);
    }

    /**
     * Return the property value associated with the given key, or
     * {@code defaultValue} if the key cannot be resolved.
     *
     * @param key          the property name to resolve
     * @param defaultValue the default value to return if no value is found
     */
    public String get(String key, String defaultValue) {
        return env.getProperty(key, defaultValue);
    }

    /**
     * Return the property value associated with the given key,
     * or {@code null} if the key cannot be resolved.
     *
     * @param key        the property name to resolve
     * @param targetType the expected type of the property value
     */
    public <T> T get(String key, Class<T> targetType) {
        T toReturn = null;
        try {
            toReturn = env.getProperty(key, targetType);
        } catch (ConversionException | IllegalArgumentException e) {
            errorLog("get", targetType, key, null);
        }
        return toReturn;
    }

    /**
     * Return the property value associated with the given key,
     * or {@code defaultValue} if the key cannot be resolved.
     *
     * @param key          the property name to resolve
     * @param targetType   the expected type of the property value
     * @param defaultValue the default value to return if no value is found
     */
    public <T> T get(String key, Class<T> targetType, T defaultValue) {
        T toReturn = defaultValue;
        try {
            toReturn = env.getProperty(key, targetType, defaultValue);
        } catch (ConversionException | IllegalArgumentException e) {
            errorLog("get", targetType, key, defaultValue);
        }
        return toReturn;
    }

    public String[] getArray(String key) {
        String p = get(key);
        if (isNotBlank(p)) {
            String[] toReturn = null;
            try {
                toReturn = p.split(",");
            } catch (PatternSyntaxException e) {
                errorLog("getArray", String[].class, key, null);
            }
            return toReturn;
        } else {
            return new String[0];
        }
    }

    public String[] getArray(String key, String[] defaultValues) {
        String[] values = getArray(key);
        if (values.length == 0 && Objects.nonNull(defaultValues)) {
            return defaultValues;
        }
        return values;
    }

    public List<String> getList(String key) {
        String[] array = getArray(key);
        return array.length == 0 ? Collections.emptyList() : Arrays.asList(array);
    }

    public List<String> getList(String key, List<String> defaultValues) {
        List<String> values = getList(key);
        if (values.isEmpty() && Objects.nonNull(defaultValues)) {
            return defaultValues;
        }
        return values;
    }

    public Integer getInteger(String key) {
        return get(key, Integer.class);
    }

    public Integer getInteger(String key, Integer defaultValue) {
        return get(key, Integer.class, defaultValue);
    }

    public Long getLong(String key, Long defaultValue) {
        return get(key, Long.class, defaultValue);
    }

    public Long getLong(String key) {
        return get(key, Long.class);
    }

    public Double getDouble(String key, Double defaultValue) {
        return get(key, Double.class, defaultValue);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return get(key, Boolean.class, defaultValue);
    }

    public List<Integer> getIntegerList(String key, List<Integer> defaultValues) {
        return getObjectList("getIntegerList", key, Integer[].class, Integer::parseInt, defaultValues);
    }

    public List<Double> getDoubleList(String key, List<Double> defaultValues) {
        return getObjectList("getDoubleList", key, Double[].class, Double::parseDouble, defaultValues);
    }

    /**
     * Gets boolean property.
     * Values 'true' / 'false' are expected.
     *
     * @param key
     * @param defaultValue
     * @return returns true when 'true' value found. returns false when 'false' found. Otherwise return <i>defaultValue</i>
     */
    public boolean getBooleanStrict(String key, boolean defaultValue) {
        String p = get(key);
        if (isNotBlank(p)) {
            return p.equalsIgnoreCase("true") || p.equalsIgnoreCase("false") ? Boolean.parseBoolean(p) : defaultValue;
        } else {
            return defaultValue;
        }
    }

    /**
     * Returns the property value given the task ID and the partial key.
     * <p>
     * For example, to retrieve the property "cron" for task ID "dog.event". The mapping
     * in the config.properties is "dog.event.cron".
     */
    public String getTaskProperty(String taskId, String key) {
        return get(String.format("%s.%s", taskId, key));
    }

    /**
     * Like {@link #getTaskProperty(String, String)} adding a default value if the property key does not exist.
     */
    public String getTaskProperty(String taskId, String key, String defaultValue) {
        return getIfBlank(getTaskProperty(taskId, key), defaultValue);
    }

    /**
     * Print error log with data type and default value used instead
     *
     * @param logId
     * @param dataTypeClass
     * @param key
     * @param defaultValue
     */
    private void errorLog(String logId, Class<?> dataTypeClass, String key, Object defaultValue) {
        LOGGER.error(AppLog.Builder.id(logId)
                             .message("Error parsing \"{}\" value for property \"{}\"", dataTypeClass.getSimpleName(), key)
                             .consequences("Using \"{}\" value", Objects.toString(defaultValue)));
    }

    /**
     * Get object list from list of string values
     *
     * @param logId
     * @param key
     * @param targetClass
     * @param mapFunction
     * @param defaultValues
     * @param <T>
     * @return
     */
    private <T> List<T> getObjectList(String logId, String key, Class<T[]> targetClass, Function<String, T> mapFunction, List<T> defaultValues) {
        try {
            return getList(key).stream().map(mapFunction).collect(Collectors.toList());
        } catch (Exception e) {
            List<T> values;
            if (Objects.isNull(defaultValues)) {
                values = Collections.emptyList();
            } else {
                values = new ArrayList<>(defaultValues);
            }
            errorLog(logId, targetClass, key, values);
            return values;
        }
    }

    /**
     * Check if the string is not null and not empty and not blank
     *
     * @param str
     * @return
     */
    private boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * Gets string if not blank or default is blank
     *
     * @param str
     * @param defaultStr
     * @return
     */
    private String getIfBlank(String str, String defaultStr) {
        return isBlank(str) ? defaultStr : str;
    }

}
