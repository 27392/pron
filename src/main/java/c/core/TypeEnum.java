package c.core;

import c.beyond.Beyond;
import c.beyond.Entry;
import c.report.Report;
import c.utils.TaskUtils;
import c.wapper.BeyondElementWrapper;
import c.wapper.ElementWrapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Getter
@RequiredArgsConstructor
public enum TypeEnum {

    /**
     * 最近精品
     */
    BOUTIQUE("https://91porn.com/v.php?category=rf&viewtype=basic"),

    /**
     * 最新
     */
    LATEST("https://91porn.com/v.php?next=watch"),

    /**
     * 本月最热
     */
    CURRENT_MONTH_HOT("https://91porn.com/v.php?category=top&viewtype=basic"),

    /**
     * 当前最热
     */
    CURRENT_HOT("https://91porn.com/v.php?category=hot&viewtype=basic"),

    /**
     * 本月收藏
     */
    CURRENT_MONTH_COLLECTION("https://91porn.com/v.php?category=tf&viewtype=basic"),

    /**
     * 原创
     */
    ORIGINAL("https://91porn.com/v.php?category=ori&viewtype=basic"),

    /**
     * 本月讨论
     */
    CURRENT_MONTH_DISCUSS("https://91porn.com/v.php?category=md&viewtype=basic"),

    /**
     * UID
     */
    UID("https://91porn.com/uvideos.php?UID=%s") {
        @Override
        public int startPage(BlockingQueue<ElementWrapper> queue) throws IOException, URISyntaxException {
            Set<String> uids;
            URL         uid  = Thread.currentThread().getContextClassLoader().getResource("uid");
            Path        path = Paths.get(uid.toURI());
            try (Stream<String> lines = Files.lines(path)) {
                uids = lines.filter(s -> !s.startsWith("-")).collect(Collectors.toCollection(LinkedHashSet::new));
            }
            if (uids.isEmpty()) {
                log.warn("uid 为空");
                return 0;
            }
            for (String id : uids) {
                TaskUtils.submit(new PageResolve(queue, this, id));
            }
            return uids.size();
        }
    },
    BEYOND("") {
        @Override
        public int startPage(BlockingQueue<ElementWrapper> queue) {
            TaskUtils.submit(() -> {

                ListIterator<Entry> iterator = new ArrayList<>(Beyond.getEntries()).listIterator(Beyond.getEntries().size());

                while (iterator.hasPrevious()) {
                    try {
                        BeyondElementWrapper beyondElementWrapper = new BeyondElementWrapper(iterator.previous());
                        queue.put(beyondElementWrapper);
                        Report.produce(beyondElementWrapper.getSourceUrl());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                log.info("BEYOND 退出");
                TaskUtils.finish();
            });
            return 1;
        }
    };

    private final String url;

    public int startPage(BlockingQueue<ElementWrapper> queue) throws IOException, URISyntaxException {
        TaskUtils.submit(new PageResolve(queue, this));
        return 1;
    }

    public int startDown(BlockingQueue<ElementWrapper> queue, int size) throws IOException, URISyntaxException {
        for (int i = 0; i < size; i++) {
            TaskUtils.submit(new Downloader(queue));
        }
        return size;
    }

    public void start(BlockingQueue<ElementWrapper> queue, int size) throws IOException, URISyntaxException, InterruptedException {
        int pageCount = this.startPage(queue);
        int downCount = this.startDown(queue, size);

        while (true) {
            TimeUnit.SECONDS.sleep(10);
            if (TaskUtils.finishCount.get() == (downCount + pageCount)) {
                log.info("{}", Report.print());
                TaskUtils.shutdown();
                break;
            }
        }
    }
    /**
     * 获取最大页码
     *
     * @param document
     * @return
     */
    public int getMaxPageSize(Document document) {
        try {
            int i = Stream.of(document.select("#paging a"), document.select("#paging span"))
                    .flatMap(Collection::stream)
                    .map(Element::text)
                    .filter(r -> r.matches("[0-9]+"))
                    .mapToInt(Integer::parseInt)
                    .max()
                    .orElse(1);
            if (i == 1) {
                return document.select("#paging div").first().children().stream()
                        .map(Element::text)
                        .filter(r -> r.matches("[0-9]+"))
                        .mapToInt(Integer::parseInt)
                        .max()
                        .orElse(1);
            } else {
                return i;
            }
        } catch (RuntimeException e) {
            return 1;
        }
    }

}