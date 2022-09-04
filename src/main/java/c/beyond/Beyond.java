package c.beyond;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author lwh
 */
@Slf4j
public class Beyond {

    public static final  String             SEPARATOR = " ## ";
    private static final Queue<Entry>       news      = new LinkedList<>();
    private static       Map<String, Entry> map;

    private static final Path path = Paths.get("beyond");

    static {
        try {
            try (Stream<String> lines = Files.lines(path)) {
                map = lines.map(r -> Entry.of(r.split(SEPARATOR)))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(Entry::getRealUrl, r -> r, (o1, o2) -> o1, LinkedHashMap::new));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Thread thread = new Thread(new Persistence());
        thread.setDaemon(true);
        thread.setName("Persistence");
        thread.start();
    }

    public static void add(double time, String url, String realUrl, String title) {
        Entry entry = new Entry(title, time, url, realUrl);
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
                        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(path.toFile(), true)))) {
                            for (int i = 0; i < news.size(); i++) {
                                Entry entry = news.poll();
                                writer.println(entry);
                            }
                        }
                    }
                }
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }

}
