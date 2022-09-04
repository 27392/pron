package c.utils;

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
public class IOHelper {

    private static final Path CACHE_DIR = Paths.get("cache");
    private static final String HTML = ".html";

    public static final LinkedHashMap<String, String> MAPPING = new LinkedHashMap<>();

    static {
        String downloadDir = System.getProperty("downloadDir");
        File file = Paths.get(downloadDir).toFile();
        readFile(file, MAPPING);
        log.info("找到文件: {}", MAPPING.size());
    }

    public static String get(String name) {
        return MAPPING.get(name);
    }

    public static void saveHtml(String name, String content) throws IOException {

//        clearCache();

        Path path = CACHE_DIR.resolve(LocalDate.now().toString());
        Path directories = Files.createDirectories(path);

        File file = new File(directories + File.separator + name + ".html");

        try (PrintWriter printWriter = new PrintWriter(new FileWriter(file, false))) {
            printWriter.write(content);
        }
    }

    public static void delHtml(String name) throws IOException {

        Path path = CACHE_DIR.resolve(LocalDate.now().toString()).resolve(name + HTML);
        Files.delete(path);
    }

    public static void clearCache() {
        LocalDate now = LocalDate.now();
        File[] files = CACHE_DIR.toFile().listFiles();
        if (Objects.isNull(files) || files.length == 0) {
            return;
        }

        for (File file : files) {
            LocalDate dir;
            try {
                dir = LocalDate.parse(file.getName());
            } catch (RuntimeException e) {
                log.error(e.getMessage(), e);
                return;
            }
            if (dir.isBefore(now)) {
                Optional.ofNullable(file.listFiles()).ifPresent(r -> {
                    Arrays.stream(r).forEach(e -> {
                        boolean delete = e.delete();
                        log.info("cache: {}, delete: {}", e.getName(), delete);
                    });
                });
            } else {
                boolean delete = file.delete();
                log.info("cache: {}, delete: {}", file.getName(), delete);
            }
        }
    }

    public static String readHtml(String name) {
        Path path = CACHE_DIR.resolve(LocalDate.now().toString());

        File file = new File(path + File.separator + name + ".html");
        if (file.exists()) {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String r;
                while ((r = reader.readLine()) != null) {
                    sb.append(r);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return sb.toString();
        }
        return null;
    }

    private static void readFile(File file, Map<String, String> map) {
        File[] files = file.listFiles();
        if (Objects.isNull(files) || files.length == 0) {
            return;
        }
        for (File f : files) {
            boolean b = f.isDirectory();
            if (b) {
                readFile(f, map);
            } else {
                map.put(f.getName(), f.getAbsolutePath());
            }
        }
    }

}
