package c.beyond;

import c.Config;
import c.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author lwh
 */
@Slf4j
public class Beyond {

    public static final  String             SEPARATOR = " ## ";
    private static final Queue<Entry>       news      = new LinkedList<>();
    private static       Map<String, Entry> map;

    private static final Path path = Paths.get(Config.getDownloadDir()).resolve("beyond.txt");

    static {
        try {
            map = FileUtils.read(FileUtils.getOrCreate(path.toFile()), r -> Entry.of(r.split(SEPARATOR)), Collectors.toMap(Entry::getUrl, r -> r, (o1, o2) -> o1, LinkedHashMap::new));
        } catch (Exception e) {
            e.printStackTrace();
        }

        Thread thread = new Thread(new Persistence());
        thread.setDaemon(true);
        thread.setName("Persistence");
        thread.start();
    }

    public static void add(LocalDate releaseDate, double time, String url, String realUrl, String title) {
        Entry entry = new Entry(title, releaseDate, time, url, realUrl);
        Entry put   = map.put(url, entry);
        if (put == null) {
            news.add(entry);
        }
    }

    public static Entry get(String url) {
        return map.get(url);
    }

    public static Collection<Entry> getEntries() {
        return map.values();
    }

    static class Persistence implements Runnable {

        @Override
        public void run() {
            try {
                while (true) {
                    TimeUnit.SECONDS.sleep(5);
                    while (!news.isEmpty()) {
                        FileUtils.writer(path.toFile(), true, (w) -> {
                            for (int i = 0; i < news.size(); i++) {
                                w.println(news.poll());
                            }
                        });
                    }
                }
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }

}
