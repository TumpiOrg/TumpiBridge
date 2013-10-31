package net.tumpi.bridge.config;

import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author Jorge Madrid Portillo (66785403)
 */
public class Config {

    private static Config INSTANCE = null;
    private Properties properties;
    private boolean isLoaded = false;
    public static final String PUERTO = "puerto";

    private Config() {
        properties = new Properties();
    }

    public static Config instance() {
        if (INSTANCE == null) {
            INSTANCE = new Config();
        }
        return INSTANCE;
    }

    public void loadProperties() throws IOException {
        if (!isLoaded) {
            isLoaded = true;
            properties.load(Config.class.getResourceAsStream("config.properties"));
        }
    }

    public String getConfigVar(String key) {
        return properties.getProperty(key).trim();
    }

    public int getPuerto() {
        return Integer.parseInt(properties.getProperty(PUERTO));
    }
}
