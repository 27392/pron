package cn.haohaoli.config;

import cn.haohaoli.core.TypeEnum;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;

/**
 * @author lwh
 */
@UtilityClass
public class Config {

    private final static Properties PROPERTIES = new Properties();

    static {
        try {
            InputStream resourceAsStream = Config.class.getClassLoader().getResourceAsStream("config.properties");
            PROPERTIES.load(resourceAsStream);

            String  ffmpegDir = Objects.requireNonNull(PROPERTIES.getProperty("ffmpegDir"), "请配置 ffmpeg 地址");
            boolean directory = Paths.get(ffmpegDir).toFile().isDirectory();
            if (!directory) {
                throw new RuntimeException(ffmpegDir + " 目录不存在");
            }

            // 创建下载目录
            Files.createDirectories(Paths.get(Config.getDownloadDir()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getDownloadDir() {
        return PROPERTIES.getProperty("downloadDir");
    }

    public String getFfmpegDir() {
        return PROPERTIES.getProperty("ffmpegDir");
    }

    public int getMaxPage() {
        return Integer.parseInt(PROPERTIES.getProperty("maxPage", "1"));
    }

    public int getMaxDuration() {
        return Integer.parseInt(PROPERTIES.getProperty("maxDuration", "25"));
    }

    public int getBeyondMaxDuration() {
        return Integer.parseInt(PROPERTIES.getProperty("beyondMaxDuration", "30"));
    }

    public int getDownloadTimeout() {
        return Integer.parseInt(PROPERTIES.getProperty("downloadTimeout", "6"));
    }

    public int getMaxHtmlCache() {
        return Integer.parseInt(PROPERTIES.getProperty("maxHtmlCache", "0"));
    }

    public int getDownCount() {
        return Integer.parseInt(PROPERTIES.getProperty("downCount", "1"));
    }

    public LocalDate getLastTime() {
        int lastTime = Integer.parseInt(PROPERTIES.getProperty("lastTime", "1"));
        return LocalDate.now().minusDays(lastTime);
    }

    public String getProxy() {
        return PROPERTIES.getProperty("proxy");
    }

    public String getProxyCommand() {
        return PROPERTIES.getProperty("proxyCommand");
    }

    public TypeEnum getType() {
        int type = Integer.parseInt(PROPERTIES.getProperty("type", "1"));
        return Arrays.stream(TypeEnum.values()).filter(r -> (r.ordinal() + 1) == type).findAny().orElse(TypeEnum.BOUTIQUE);
    }
}
