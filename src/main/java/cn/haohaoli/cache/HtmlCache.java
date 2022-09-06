package cn.haohaoli.cache;

import cn.haohaoli.cmmon.Const;
import cn.haohaoli.utils.RegexUtils;
import cn.haohaoli.config.Config;
import cn.haohaoli.utils.FileUtils;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

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

    private final Path CACHE_DIR = Paths.get(Config.getDownloadDir()).resolve(Const.HTML_DIR_NAME);

    private final Map<String, Value> MAPPING = new LinkedHashMap<>();

    static {

        try {
            FileUtils.scanFile(CACHE_DIR, f -> f.getName().endsWith(Const.HTML_SUFFIX), f -> MAPPING.put(f.getName(), new Value(f, null)));
            log.info("找到html文件: {}", MAPPING.size());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 清理缓存
        clear(Config.getMaxHtmlCache());
    }

    /**
     * 保存
     *
     * @param name
     * @param content
     * @throws IOException
     */
    public void save(String name, String content) throws IOException {
        String cacheKey = getCacheKey(name);

        Path path        = CACHE_DIR.resolve(dir(content).toString());
        Path directories = Files.createDirectories(path);

        File file = new File(directories + File.separator + cacheKey);
        FileUtils.writer(file, false, content);

        MAPPING.put(cacheKey, new Value(file, content));
    }

    /**
     * 清除缓存
     *
     * @param count
     */
    public void clear(int count) {
        if (count <= 0) {
            return;
        }
        if (MAPPING.size() <= count) {
            return;
        }
        File[] files = FileUtils.getFilesBySortCreate(CACHE_DIR.toFile());
        if (Objects.isNull(files) || files.length == 0) {
            return;
        }

        int removeCount = MAPPING.size() - count;
        int delCount    = 0;
        for (File file : files) {
            File[] fs = FileUtils.getFilesBySortCreate(file);
            if (file.isDirectory() && (fs != null && fs.length != 0)) {
                int c = 0;
                for (File f : fs) {
                    if (delCount >= removeCount) {
                        log.info("cache: 删除数量: {}", delCount);
                        return;
                    }
                    boolean delete = FileUtils.delete(f);
                    if (delete) {
                        MAPPING.remove(f.getName());
                    }
                    log.info("cache: {}, delete: {}", f.getName(), delete);

                    delCount++;
                    c++;
                }
                if (c == fs.length) {
                    boolean delete = FileUtils.delete(file);
                    log.info("cache: {}, delete: {}", file.getName(), delete);
                }
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
        Value  value    = MAPPING.get(cacheKey);

        if (value != null) {
            if (value.text == null) {
                String content = FileUtils.readToString(value.file);
                if (content != null) {
                    value.text = content;
                }
            }
            return value.text;
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
        String replace = url.replace(Const.HOST, "");
        if (replace.startsWith("view_video.php")) {
            replace = RegexUtils.id(replace);
        }
        return replace + Const.HTML_SUFFIX;
    }

    /**
     * 日期
     *
     * @param content
     * @return
     */
    private LocalDate dir(String content) {
        Document parse  = Jsoup.parse(content);
        Elements select = parse.select(".title-yakov");
        if (select.size() == 0) {
            return LocalDate.now();
        }
        String text = parse.select(".title-yakov").first().text();
        return LocalDate.parse(text);
    }

    @AllArgsConstructor
    static class Value {
        private final File   file;
        private       String text;
    }
}
