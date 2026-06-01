package api.configs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static final Config INSTANCE = new Config();
    private final Properties properties = new Properties();

    private Config(){
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("config.properties not found in resources");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Fail to load confog.properties", e);
        }
    }

    public static String getProperty(String key) {
        // Приоритет 1: переменные системы baseApiUrl
        String systemValue = System.getProperty(key);
        if (systemValue != null) {
            return systemValue;
        }
        // Приоритет 2: переменные окружения BASEAPIURL
        String envKey = key.toUpperCase().replace('.', '_');
        String envValue = System.getenv(envKey);
        if (envValue != null){
            return envValue;
        }
        // Приоритет 3: config.properties
        return INSTANCE.properties.getProperty(key);
    }
}
