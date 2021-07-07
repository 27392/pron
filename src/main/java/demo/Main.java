package demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY;

/**
 * Created on 06/20 2021.
 *
 * @author Bennie
 */
public class Main {

    static {
        System.setProperty(DEFAULT_LOG_LEVEL_KEY, "INFO");
    }

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        download();
    }

    static void download() throws Exception {
        Path            path = Paths.get(ClassLoader.getSystemResource("dl_list.txt").toURI());
        ExecutorService pool = Executors.newCachedThreadPool();

        try (Stream<String> lines = Files.lines(path)) {
            List<Downloader> collect = lines
                    .filter(line -> !line.startsWith("#"))
                    .distinct()
                    .map(Downloader::new)
                    .collect(Collectors.toList());
            pool.invokeAll(collect);
        } catch (Exception e) {
            logger.error("error", e);
        }
    }

}