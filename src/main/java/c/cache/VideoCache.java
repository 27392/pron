package c.cache;

import c.Config;
import c.utils.FileUtils;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Paths;
import java.util.*;

/**
 * @author lwh
 */
@Slf4j
@UtilityClass
public class VideoCache {

    public final String SUFFIX = ".mp4";

    private final Map<String, String> MAPPING = new LinkedHashMap<>();

    static {
        FileUtils.scanFile(Paths.get(Config.getDownloadDir()), f -> f.getName().endsWith(SUFFIX), f -> MAPPING.put(f.getName(), f.getAbsolutePath()));
        log.info("找到视频文件: {}", MAPPING.size());
    }

    public String get(String name) {
        return MAPPING.get(name + SUFFIX);
    }

}
