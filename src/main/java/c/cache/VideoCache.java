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

    private final Path CACHE_DIR = Paths.get(Config.getDownloadDir());

    static {
        FileUtils.scanFile(CACHE_DIR, f -> f.getName().endsWith(SUFFIX), f -> MAPPING.put(f.getName(), f.getAbsolutePath()));
        log.info("找到视频文件: {}", MAPPING.size());
    }

    public String get(String name) {
        return MAPPING.get(name + SUFFIX);
    }

    public boolean delete(String title) throws IOException {
        Path path = CACHE_DIR.resolve(LocalDate.now().toString())
                .resolve(title + SUFFIX);
        return path.toFile().delete();
    }

}
