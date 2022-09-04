package c;

import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author lwh
 */
@UtilityClass
public class Config {

    private final static Properties PROPERTIES = new Properties();

    static {
        InputStream resourceAsStream = Config.class.getClassLoader().getResourceAsStream("config.properties");
        try {
            PROPERTIES.load(resourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getDownloadDir() {
        return PROPERTIES.getProperty("downloadDir");
    }

    public int getMaxPage() {
        return Integer.parseInt(PROPERTIES.getProperty("maxPage","5"));
    }

    public int getMaxDuration() {
        return Integer.parseInt(PROPERTIES.getProperty("maxDuration","25"));
    }

    public int getDownloadTimeout() {
        return Integer.parseInt(PROPERTIES.getProperty("downloadTimeout","6"));
    }

    public String getWebDriver() {
        return System.getProperty("webdriver");
    }

    public int getCleanHtmlCache() {
        return Integer.parseInt(PROPERTIES.getProperty("cleanHtmlCache","6"));
    }
}
