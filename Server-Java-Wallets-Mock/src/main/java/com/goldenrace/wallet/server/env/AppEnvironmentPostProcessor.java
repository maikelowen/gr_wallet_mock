package com.goldenrace.wallet.server.env;

import com.goldenrace.wallet.server.properties.logging.AppLog;
import com.goldenrace.wallet.server.properties.logging.AppLogger;
import com.goldenrace.wallet.server.properties.logging.IAppLogger;
import org.apache.commons.io.FilenameUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.PropertiesPropertySourceLoader;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.boot.env.RandomValuePropertySource;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.SystemEnvironmentPropertySource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.StringJoiner;

/**
 * This class will be deleted when the project is updated to V7. See /main/develop/spring_boot_migration/[TEST]openjdk17
 * See "sell.wrong.response" property
 */
public class AppEnvironmentPostProcessor implements EnvironmentPostProcessor, ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private static final IAppLogger LOGGER = AppLogger.getLogger(AppEnvironmentPostProcessor.class);

    private static final String GOLDENRACE_SERVER_CONFIG_SOURCE_NAME = "goldenraceServerConfig";
    private static final String GOLDENRACE_HOME_CONFIG_SOURCE_NAME   = "goldenraceHomeConfig";
    private static final String GOLDENRACE_ENVIRONMENT_SOURCE_NAME   = "goldenraceEnvironment";
    private static final String GOLDENRACE_CONFIG_SOURCE_NAME        = "goldenraceConfig";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {

        LOGGER.debug("Starting to load all properties...");

        MutablePropertySources  propertySources         = environment.getPropertySources();
        ResourcePatternResolver resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(new DefaultResourceLoader());

        // Order to add the result property sources
        // List in order after finish the process:StandardEnvironment
        // After step: a RandomValuePropertySource that has properties only in random.*
        //   1. goldenraceServerConfig:
        //        file:${server.config.path}/**/* or file:./config/**/* if ${server.config.path} not exists (*.{properties,yml,yaml})
        //   2. goldenraceHomeConfig:
        //        file:{home}/.goldenrace/**/* (*.{properties,yml,yaml})
        //   3. goldenraceEnvironment:
        //        GR_{NAMESPACE}_{SUBPATH}_{KEY}=value environment properties
        // At the end:
        //   4. goldenraceConfig:
        //        classpath*:/config/**/* (*.{properties,yml,yaml}) (Unordered files)

        // Load userHomeConfig: Process {home}/.goldenrace/**/* (*.{properties,yml,yaml})
        CompositePropertySource goldenraceHomeConfigPropertySources = processPropertyPath(
                resourcePatternResolver, GOLDENRACE_HOME_CONFIG_SOURCE_NAME, getLocation(System.getProperty("user.home"), ".goldenrace"));

        // Load goldenraceEnvironment: Load environment properties
        PropertiesPropertySource goldenraceEnvironmentPropertySource = processEnvironmentProperties(propertySources);

        // Load goldenraceConfig: Process local execution folder (*.{properties,yml,yaml})
        CompositePropertySource goldenraceConfigPropertySources = processPropertyLocation(
                resourcePatternResolver, GOLDENRACE_CONFIG_SOURCE_NAME, "/config", true);

        // Add to list in order

        // START INSERT PROPERTIES AFTER RANDOM VALUE PROPERTY SOURCE PROPERTIES
        // Inverter order because inserted after random map of properties
        // The random map of properties is the first before application and application-profiles properties

        // 3. goldenraceEnvironment
        addPropertySourceAfterRandomValuePropertySource(propertySources, goldenraceEnvironmentPropertySource);

        // 2. goldenraceHomeConfig
        addPropertySourceAfterRandomValuePropertySource(propertySources, goldenraceHomeConfigPropertySources);

        // END INSERT PROPERTIES AFTER RANDOM VALUE PROPERTY SOURCE PROPERTIES

        // START INSERT CONFIG PROPERTIES AT THE END OF EVERYTHING
        // Once everything is loaded, as a last resource, default properties are assigned

        // 4. goldenraceConfig
        propertySources.addLast(goldenraceConfigPropertySources);

        // END INSERT PROPERTIES AFTER APPLICATION AND APPLICATION PROFILES PROPERTIES

        // Found external location path in all properties already loaded or use "./config"
        String goldenraceServerConfigPath = environment.getProperty("server.config.path", getLocation(".", "config"));
        // Get goldenraceServerConfig: Process classpath folder for default properties out of jar
        CompositePropertySource goldenraceServerConfigPropertySources = processPropertyPath(
                resourcePatternResolver, GOLDENRACE_SERVER_CONFIG_SOURCE_NAME, goldenraceServerConfigPath);
        // Finally, add goldenraceServerConfig before to goldenraceHomeConfig
        // 1. goldenraceServerConfig
        addPropertySourceAfterRandomValuePropertySource(propertySources, goldenraceServerConfigPropertySources);

        // Fix properties by overwritten prefixes, to override properties of a framework or library with your own prefix,
        // replacing the prefix used for the application with the original prefix.
        overwrittenPrefixes(propertySources, goldenraceConfigPropertySources);

        LOGGER.debug("End of property loading");

    }

    /**
     * Log message
     *
     * @param logId   the log identifier
     * @param message the message
     */
    private void log(String logId, String message) {
        LOGGER.debug("[{}] {}", logId, message);
    }

    /**
     * Log begin message. Example "[BEGIN] message"
     *
     * @param message the message
     */
    private void beginLog(String message) {
        log("BEGIN", message);
    }

    /**
     * Log begin message. Example "[END] message"
     *
     * @param message the message
     */
    private void endLog(String message) {
        log("END", message);
    }

    /**
     * Get location joining the paths with {@link File#separator}
     *
     * @param paths the paths
     * @return the location
     */
    private String getLocation(String... paths) {
        StringJoiner sj = new StringJoiner("/");
        for (String path : paths) {
            sj.add(path);
        }
        return sj.toString();
    }

    /**
     * Add {@link PropertySource} to environment {@link MutablePropertySources} after to @PropertySource annotations
     *
     * @param propertySources
     * @param propertySource
     */
    private void addPropertySourceAfterRandomValuePropertySource(MutablePropertySources propertySources, PropertySource<?> propertySource) {
        propertySources.addAfter(RandomValuePropertySource.RANDOM_PROPERTY_SOURCE_NAME, propertySource);
    }

    /**
     * Load from environment the GR_{NAMESPACE}_{SUBPATH}_{KEY}=value properties before "" source properties
     *
     * @param propertySources the property sources
     */
    private PropertiesPropertySource processEnvironmentProperties(MutablePropertySources propertySources) {
        String logMessage = "Loading gr environment properties";
        beginLog(logMessage);
        PropertySource<?> propertySource = propertySources.get(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
        if (propertySource == null) {
            return new PropertiesPropertySource(GOLDENRACE_ENVIRONMENT_SOURCE_NAME, new Properties());
        }
        SystemEnvironmentPropertySource systemEnvironmentPropertySource = (SystemEnvironmentPropertySource) propertySource;
        Properties                      props                           = new Properties();
        for (Map.Entry<String, Object> sourceEntry : systemEnvironmentPropertySource.getSource().entrySet()) {
            String propertyKey = sourceEntry.getKey().toLowerCase();
            if (propertyKey.startsWith("goldenrace_") || propertyKey.startsWith("gr_")) {
                String key   = propertyKey.replace("_", ".");
                String value = Objects.toString(sourceEntry.getValue());
                LOGGER.debug("Loading gr environment:\t{}\t:{}", key, value);
                props.setProperty(key, value);
            }
        }
        if (props.isEmpty()) {
            LOGGER.debug("GR environment is empty");
        }
        endLog(logMessage);
        return new PropertiesPropertySource(GOLDENRACE_ENVIRONMENT_SOURCE_NAME, props);
    }

    /**
     * Get location pattern
     *
     * @param locationPath
     * @param isLocal
     * @return the location pattern
     */
    private String getLocationPattern(String locationPath, boolean isLocal) {
        StringBuilder sb = new StringBuilder();
        if (isLocal) {
            sb.append("classpath*:").append(locationPath).append("/**/*");
        } else {
            sb.append("file:").append(getLocation(locationPath, "**", "*"));
        }
        return sb.toString();
    }

    /**
     * Process a map of {@link Resource[]} to load properties in a {@link CompositePropertySource}
     *
     * @param locationPropertySources
     * @param sourceLoaderMap
     * @param resourceMap
     * @param isLocal                 If false, resource list will be ordered
     * @throws IOException
     */
    private void processPropertyResources(CompositePropertySource locationPropertySources,
            Map<String, PropertySourceLoader> sourceLoaderMap, Map<String, Resource[]> resourceMap, boolean isLocal) throws IOException {
        List<PropertySource<?>> propertySources = new ArrayList<>();
        for (Map.Entry<String, PropertySourceLoader> sourceLoaderEntry : sourceLoaderMap.entrySet()) {
            String               resourceType         = sourceLoaderEntry.getKey();
            PropertySourceLoader propertySourceLoader = sourceLoaderEntry.getValue();
            Resource[]           resources            = resourceMap.get(resourceType);
            if (resources != null) {
                Map<String, String> resourceDescriptions = new HashMap<>();
                for (Resource resource : resources) {
                    if (resource.getFilename() != null) {
                        String baseName = FilenameUtils.getBaseName(resource.getFilename());
                        resourceDescriptions.put(baseName, resource.getDescription());
                        propertySources.addAll(propertySourceLoader.load(baseName, resource));
                    }
                }
                for (PropertySource<?> propertySource : propertySources) {
                    // Logging properties
                    String sourceName       = propertySource.getName();
                    String logSourceMessage = String.format("Loading \"%s\" properties", resourceDescriptions.get(sourceName));
                    beginLog(logSourceMessage);
                    if (propertySource instanceof EnumerablePropertySource<?>) {
                        EnumerablePropertySource<?> enumerablePropertySource = (EnumerablePropertySource<?>) propertySource;
                        for (String propertyName : enumerablePropertySource.getPropertyNames()) {
                            LOGGER.debug("Loading \"{}\" properties - {}:{}", sourceName, propertyName, enumerablePropertySource.getProperty(propertyName));
                        }
                    }
                    endLog(logSourceMessage);
                }
            }
        }
        if (!isLocal) {
            propertySources.sort(Comparator.comparing(PropertySource::getName));
        }
        propertySources.forEach(locationPropertySources::addPropertySource);
    }

    /**
     * Get location files pattern. Example: classpath:/config/** /*.properties
     *
     * @param resourceType
     * @param locationPattern
     * @return
     */
    private String getLocationFilesPattern(String resourceType, String locationPattern) {
        return locationPattern + "." + resourceType;
    }

    /**
     * Resolver and add resources in the map
     *
     * @param resourcePatternResolver
     * @param sourceLoaderMap
     * @param resourceMap
     * @param locationPattern
     * @throws IOException
     */
    private void addResources(ResourcePatternResolver resourcePatternResolver,
            Map<String, PropertySourceLoader> sourceLoaderMap, Map<String, Resource[]> resourceMap, String locationPattern) throws IOException {
        for (String resourceType : sourceLoaderMap.keySet()) {
            Resource[] resources = resourcePatternResolver.getResources(getLocationFilesPattern(resourceType, locationPattern));
            if (resources.length > 0) {
                resourceMap.put(resourceType, resources);
            }
        }
    }

    /**
     * Resolver by type and add resources in the map
     *
     * @param logId
     * @param resourcePatternResolver
     * @param propertySourceName
     * @param locationPattern
     * @param isLocal
     */
    private CompositePropertySource loadPropertySources(String logId, ResourcePatternResolver resourcePatternResolver, String propertySourceName,
            String locationPattern, boolean isLocal) {
        CompositePropertySource           locationPropertySources        = new CompositePropertySource(propertySourceName);
        PropertiesPropertySourceLoader    propertiesPropertySourceLoader = new PropertiesPropertySourceLoader();
        YamlPropertySourceLoader          yamlPropertySourceLoader       = new YamlPropertySourceLoader();
        Map<String, PropertySourceLoader> sourceLoaderMap                = new HashMap<>();
        sourceLoaderMap.put("properties", propertiesPropertySourceLoader);
        sourceLoaderMap.put("yaml", yamlPropertySourceLoader);
        sourceLoaderMap.put("yml", yamlPropertySourceLoader);
        try {
            Map<String, Resource[]> resourceMap = new HashMap<>();
            addResources(resourcePatternResolver, sourceLoaderMap, resourceMap, locationPattern);
            if (resourceMap.isEmpty()) {
                LOGGER.debug("Location \"{}\" has no resources", locationPattern);
            } else {
                processPropertyResources(locationPropertySources, sourceLoaderMap, resourceMap, isLocal);
                if (locationPropertySources.getPropertyNames().length == 0) {
                    LOGGER.debug("Location \"{}\" is empty", locationPattern);
                }
            }
        } catch (IOException ex) {
            LOGGER.error(AppLog.Builder.id(logId)
                                 .message("Cannot load configuration files from location \"{}\"", locationPattern)
                                 .consequences("Application cannot get needed configuration properties"), ex);
            throw new IllegalStateException(String.format("Cannot load configuration files from location \"%s\"", locationPattern));
        }
        return locationPropertySources;
    }

    /**
     * Process all properties in a location
     *
     * @param propertySourceName
     * @param location
     * @param isLocal            Indicates if the location is local (classpath*:) or not (file:)
     * @return the property source
     */
    private CompositePropertySource processPropertyLocation(ResourcePatternResolver resourcePatternResolver,
            String propertySourceName, String location, boolean isLocal) {
        String logId           = "processPropertyFolder";
        String locationPattern = getLocationPattern(location, isLocal);
        String logMessage      = String.format("Loading \"%s\" location properties", locationPattern);
        beginLog(logMessage);
        CompositePropertySource locationPropertySources = loadPropertySources(logId, resourcePatternResolver, propertySourceName, locationPattern, isLocal);
        endLog(logMessage);
        return locationPropertySources;
    }

    /**
     * Process all properties in an external location
     *
     * @param propertySourceName
     * @param location
     * @return the property source
     */
    private CompositePropertySource processPropertyPath(ResourcePatternResolver resourcePatternResolver,
            String propertySourceName, String location) {
        return processPropertyLocation(resourcePatternResolver, propertySourceName, location, false);
    }

    /**
     * Overwritten prefixes, not deleting the properties themselves, but adding the new ones needed based on the new prefix
     * by substituting the one to be discarded.
     *
     * @param propertySources                 the property sources
     * @param goldenraceConfigPropertySources the "goldenraceConfig" property source
     */
    private void overwrittenPrefixes(MutablePropertySources propertySources, CompositePropertySource goldenraceConfigPropertySources) {
        Map<String, Properties> propertiesMap = new HashMap<>();
        String[]                propertyNames = goldenraceConfigPropertySources.getPropertyNames();
        for (String propertyName : propertyNames) {
            if (propertyName.matches("^goldenrace\\.properties\\.overwritten-prefix\\.([^.]+)\\.from$")) {
                String goldenracePrefix       = (String) goldenraceConfigPropertySources.getProperty(propertyName);
                String originalPrefixProperty = propertyName.replaceFirst("\\.from", ".to");
                String originalPrefix         = (String) goldenraceConfigPropertySources.getProperty(originalPrefixProperty);
                for (PropertySource<?> source : propertySources) {
                    if (source instanceof EnumerablePropertySource) {
                        EnumerablePropertySource<?> enumerablePropertySource = (EnumerablePropertySource<?>) source;
                        for (String sourcePropertyName : enumerablePropertySource.getPropertyNames()) {
                            Object value = enumerablePropertySource.getProperty(sourcePropertyName);
                            if (value != null && sourcePropertyName.startsWith(goldenracePrefix + ".")) {
                                String realPrefix = sourcePropertyName.replaceFirst(goldenracePrefix + "\\.", originalPrefix + ".");
                                propertiesMap.computeIfAbsent(source.getName(), k -> new Properties()).put(realPrefix, value);
                            }
                        }
                    }
                }
            }
        }
        propertiesMap.forEach((sourceName, properties) -> {
            PropertiesPropertySource propertySource = new PropertiesPropertySource(sourceName + "_fixed", properties);
            propertySources.addBefore(sourceName, propertySource);
        });
    }

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
    }

}

