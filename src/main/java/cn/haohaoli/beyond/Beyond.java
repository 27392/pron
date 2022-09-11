package cn.haohaoli.beyond;

import cn.haohaoli.cache.VideoCache;
import cn.haohaoli.cmmon.Const;
import cn.haohaoli.config.Config;
import cn.haohaoli.utils.FileUtils;
import cn.haohaoli.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author lwh
 */
@Slf4j
public class Beyond {

    private static Map<String, Entry> map;

    private static final Queue<Entry> NEWS_QUEUE = new LinkedList<>();

    private static final Path PATH = Paths.get(Config.getDownloadDir()).resolve("beyond.txt");

    private static final Thread thread;

    static {
        try {
            map = FileUtils.read(FileUtils.getOrCreate(PATH.toFile()), r -> Entry.of(r.split(Const.BEYOND_SEPARATOR)), Collectors.toMap(Entry::getUrl, r -> r, (o1, o2) -> o1, LinkedHashMap::new));
        } catch (Exception e) {
            e.printStackTrace();
        }

        log.info("找到超长视频: {}", map.size());
    }

    static {
        thread = new Thread(new Persistence());
        thread.setDaemon(true);
        thread.setName("Persistence");
        thread.start();
    }

    public static void add(Entry entry) {
        Entry put = map.put(entry.getUrl(), entry);
        if (put == null) {
            NEWS_QUEUE.add(entry);
        }
    }

    public static void remove(String url) {
        map.remove(url);
    }

    public static Entry get(String url) {
        return map.get(url);
    }

    public static Collection<Entry> getEntries() {
        return Collections.unmodifiableCollection(map.values());
    }

    public static void refresh() throws IOException {
        thread.interrupt();

        LocalDate now = LocalDate.now();
        List<Entry> collect = getEntries().stream()
                .filter(r -> now.toEpochDay() - r.getReleaseDate().toEpochDay() < 7)
                .filter(r -> VideoCache.get(RegexUtils.id(r.getUrl())) == null)
                .sorted(Comparator.comparing(Entry::getReleaseDate).thenComparing(Entry::getTime))
                .collect(Collectors.toList());

        if (collect.size() == 0) {
            FileUtils.delete(PATH.toFile());
        } else {
            FileUtils.writer(PATH.toFile(), false, collect);
        }
    }

    static class Persistence implements Runnable {

        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException e) {
                        log.debug("打断线程");
                        break;
                    }
                    while (!NEWS_QUEUE.isEmpty()) {
                        FileUtils.writer(PATH.toFile(), true, (w) -> {
                            for (int i = 0; i < NEWS_QUEUE.size(); i++) {
                                w.println(NEWS_QUEUE.poll());
                            }
                        });
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

