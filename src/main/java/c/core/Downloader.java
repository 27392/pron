package c.core;

import c.Config;
import c.cache.VideoCache;
import c.event.*;
import c.utils.EventPublisher;
import c.wapper.ElementWrapper;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.concurrent.*;

/**
 * Created on 06/27 2021.
 *
 * @author Bennie
 */
@Slf4j
public class Downloader implements Runnable {

    private static final String COMMAND = "export https_proxy=http://127.0.0.1:7890 http_proxy=http://127.0.0.1:7890 all_proxy=socks5://127.0.0.1:7890 && ffmpeg -y -i '%s' -acodec copy -vcodec copy '%s'";

    private final BlockingQueue<ElementWrapper> queue;

    public Downloader(BlockingQueue<ElementWrapper> queue) {
        this.queue = queue;
    }

    private int outputToMp4(Path dir, String title, String m3u8Url) throws InterruptedException, IOException {

        Path directories = Files.createDirectories(dir).resolve(title + VideoCache.SUFFIX);

        String cmd = String.format(COMMAND, m3u8Url, directories.toString());

        ProcessBuilder builder = new ProcessBuilder("/bin/sh", "-c", cmd);
        Process        p       = builder.start();
        int            i       = p.waitFor();
        if (i != 0) {
            log.warn("cmd: {}", cmd);
        }
        return i;
    }


    @Override
    public void run() {
        log.info("开始");

        try {
            while (true) {
                ElementWrapper element = queue.poll(30, TimeUnit.SECONDS);
                if (element == null) {
                    break;
                }

                String title = element.getTitle();
                log.debug("get: {}", title);

                if (element.exist()) {
                    log.debug("跳过: {}", title);
                    EventPublisher.publish(new VideoSkipEvent(element));
                    continue;
                }
                double duration = element.getDuration();
                if (duration > Config.getMaxDuration()) {
                    log.debug("时间超长 {} 分钟: [{}]", duration, title);
                    EventPublisher.publish(new VideoExpiredEvent(element));
                    continue;
                }

                String    url         = element.getUrl();
                LocalDate releaseDate = element.getReleaseDate();
                if (Config.getLastTime().isAfter(releaseDate)) {
                    log.debug("时间超出: 发布时间: [{}], 规定时间: [{}], 名称: [{}], 来源: [{}], 地址: [{}]", releaseDate, Config.getLastTime(), title, element.getSourceUrl(), url);
                    EventPublisher.publish(new VideoExpiredEvent(element));
                    continue;
                }

                Path path    = element.downDir();
                long timeout = element.timeout();
                long start   = System.currentTimeMillis();
                log.info("开始下载: 发布时间: [{}], [超时: {} / 总时长: {}], 名称: [{}], 来源: [{}], 地址: [{}]", releaseDate, timeout, duration, title, element.getSourceUrl(), url);

                String m3u8Src = element.getRealUrl();
                if (m3u8Src == null) {
                    log.debug("跳过: {}", title);
                    EventPublisher.publish(new VideoSkipEvent(element));
                }

                FutureTask<Integer> integerFutureTask = new FutureTask<>(() -> outputToMp4(path, title, m3u8Src));
                Thread              thread            = new Thread(integerFutureTask, "down-");
                thread.start();

                try {
                    Integer i = integerFutureTask.get(element.timeout(), TimeUnit.MINUTES);
                    if (i == 0) {
                        log.info("下载完成: 耗时: [{}], 发布时间: [{}], 名称: [{}], 地址: [{}]", watch(start), releaseDate, title, url);
                        EventPublisher.publish(new VideoDownSuccessEvent(element));
                    } else {
                        log.error("下载失败: 耗时: [{}], 发布时间: [{}], 名称 [{}], 地址: [{}]", watch(start), releaseDate, title, url);
                        EventPublisher.publish(new VideoDownFailEvent(element));
                    }
                } catch (TimeoutException e) {
                    EventPublisher.publish(new VideoDownTimeoutEvent(element));
                    log.error("下载超时删除: 耗时: [{}], 名称: [{}], 地址: [{}]", watch(start), title, url);
                } catch (Exception e) {
                    log.error("下载失败", e);
                    EventPublisher.publish(new VideoDownFailEvent(element));
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        log.info("Download -> 退出");
        EventPublisher.publish(new DownloaderFinishEvent());
    }

    private String watch(long start) {
        double d = ((System.currentTimeMillis() - start) / 1000d) / 60d;
        return String.format("%.2f", d);
    }

}
