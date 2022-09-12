package cn.haohaoli.cache;

import cn.haohaoli.cmmon.Const;
import cn.haohaoli.config.Config;
import cn.haohaoli.utils.FileUtils;
import cn.haohaoli.wapper.ElementWrapper;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

import static cn.haohaoli.cmmon.Const.VIDEO_SUFFIX;

/**
 * @author lwh
 */
@Slf4j
@UtilityClass
public class VideoCache {

    private final Map<String, String> MAPPING = new LinkedHashMap<>();

    public final Path CACHE_DIR = Paths.get(Config.getDownloadDir()).resolve(Const.VIDEO_DIR_NAME);

    static {
        try {
            FileUtils.scanFile(CACHE_DIR, f -> f.getName().endsWith(VIDEO_SUFFIX), f -> {
                String[] split = f.getName().split(" - ");
                MAPPING.put(split[1], f.getAbsolutePath());
            });
            log.info("找到视频文件: {}", MAPPING.size());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String get(String id) {
        return MAPPING.get(id + VIDEO_SUFFIX);
    }

    public void add(ElementWrapper wrapper) {
        MAPPING.put(wrapper.getId() + VIDEO_SUFFIX, wrapper.downDir().resolve(wrapper.getFieldName() + VIDEO_SUFFIX).toString());
    }

    public boolean delete(LocalDate releaseDate, String title) throws IOException {
        Path path = CACHE_DIR.resolve(releaseDate.toString())
                .resolve(title + VIDEO_SUFFIX);
        return FileUtils.delete(path.toFile());
    }

}
