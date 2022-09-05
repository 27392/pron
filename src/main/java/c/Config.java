package c;

import c.core.TypeEnum;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Arrays;
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
        return Integer.parseInt(PROPERTIES.getProperty("maxPage", "5"));
    }

    public int getMaxDuration() {
        return Integer.parseInt(PROPERTIES.getProperty("maxDuration", "25"));
    }

    public int getDownloadTimeout() {
        return Integer.parseInt(PROPERTIES.getProperty("downloadTimeout", "6"));
    }

    public String getWebDriver() {
        return PROPERTIES.getProperty("webdriver");
    }

    public int getMaxHtmlCache() {
        return Integer.parseInt(PROPERTIES.getProperty("maxHtmlCache", "6"));
    }

    public int getDownCount() {
        return Integer.parseInt(PROPERTIES.getProperty("downCount", "6"));
    }

    public LocalDate getLastTime() {
        int lastTime = Integer.parseInt(PROPERTIES.getProperty("lastTime", "7"));
        return LocalDate.now().minusDays(lastTime);
    }

    public TypeEnum getType() {
        int type = Integer.parseInt(PROPERTIES.getProperty("type", "1"));
        return Arrays.stream(TypeEnum.values()).filter(r -> (r.ordinal() + 1) == type).findAny().orElse(TypeEnum.BOUTIQUE);
    }
}
