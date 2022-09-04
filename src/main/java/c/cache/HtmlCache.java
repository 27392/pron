package c.cache;

import c.Config;
import c.utils.FileUtils;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

/**
 * @author lwh
 */
@Slf4j
@UtilityClass
public class HtmlCache {

    public final String SUFFIX = ".html";
    public final String PREFIX = "https://91porn.com/";

    private final Path CACHE_DIR = Paths.get("cache");

    private final Map<String, String> MAPPING = new LinkedHashMap<>();

    static {
        FileUtils.scanFile(CACHE_DIR, f -> f.getName().endsWith(SUFFIX), f -> {
            MAPPING.put(f.getParentFile().getName() + ":" + f.getName(), "null");
        });
        log.info("找到html文件: {}", MAPPING.size());

        // 清理缓存
        clear(Config.getCleanHtmlCache());
    }

    public void save(String name, String content) throws IOException {
        String cacheKey = getCacheKey(name);

        Path path        = CACHE_DIR.resolve(LocalDate.now().toString());
        Path directories = Files.createDirectories(path);

        File file = new File(directories + File.separator + cacheKey.split(":")[1]);
        FileUtils.writer(file, false, content);
    }

    /**
     * 删除文件
     *
     * @param name
     * @throws IOException
     */
    public static boolean delete(String name) throws IOException {
        Path path = CACHE_DIR.resolve(LocalDate.now().toString()).resolve(getCacheKey(name).split(":")[1]);
        return path.toFile().delete();
    }

    /**
     * 清除缓存
     *
     * @param month
     */
    public void clear(int month) {
        LocalDate localDate = LocalDate.now().minusMonths(month);
        File[]    files     = CACHE_DIR.toFile().listFiles();
        if (Objects.isNull(files) || files.length == 0) {
            return;
        }

        for (File file : files) {
            if (!file.isDirectory()) {
                continue;
            }
            LocalDate dir;
            try {
                dir = LocalDate.parse(file.getName());
            } catch (RuntimeException e) {
                log.error(e.getMessage(), e);
                return;
            }
            if (dir.isBefore(localDate)) {
                Optional.ofNullable(file.listFiles()).ifPresent(r -> {
                    Arrays.stream(r).forEach(e -> {
//                        boolean delete = e.delete();
                        log.info("cache: {}, delete: {}", e.getName(), false);
                    });
                });
            }
        }
    }

    /**
     * 获取
     *
     * @param name
     * @return
     */
    public String get(String name) {
        String cacheKey = getCacheKey(name);
        String value    = MAPPING.get(cacheKey);
        if (value != null) {
            if (value.equals("null")) {
                Path   path    = CACHE_DIR.resolve(LocalDate.now().toString()).resolve(cacheKey.split(":")[1]);
                String content = FileUtils.readToString(path.toFile());
                if (content != null) {
                    MAPPING.put(cacheKey, content);
                }
            }
            return MAPPING.get(cacheKey);
        }
        return null;
    }

    /**
     * 获取缓存key
     *
     * @param url
     * @return
     */
    public String getCacheKey(String url) {
        return LocalDate.now().toString() + ":" + url.replace(PREFIX, "") + SUFFIX;
    }

}
