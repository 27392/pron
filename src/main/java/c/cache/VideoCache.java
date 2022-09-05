package c.cache;

import c.Config;
import c.utils.FileUtils;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

/**
 * @author lwh
 */
@Slf4j
@UtilityClass
public class VideoCache {

    public final String SUFFIX = ".mp4";

    private final Map<String, String> MAPPING = new LinkedHashMap<>();

    public final Path CACHE_DIR = Paths.get(Config.getDownloadDir()).resolve("video");

    static {
        FileUtils.scanFile(CACHE_DIR, f -> f.getName().endsWith(SUFFIX), f -> {
            String[] split = f.getName().split(" - ");
            MAPPING.put(split[1], f.getAbsolutePath());
        });
        log.info("找到视频文件: {}", MAPPING.size());
    }

    public String get(String name) {
        String[] split = name.split(" - ");
        return MAPPING.get(split[1] + SUFFIX);
    }

    public boolean delete(String title) throws IOException {
        Path path = CACHE_DIR.resolve(LocalDate.now().toString())
                .resolve(title + SUFFIX);
        return path.toFile().delete();
    }

}
