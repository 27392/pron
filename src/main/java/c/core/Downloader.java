package c.core;

import c.Config;
import c.cache.HtmlCache;
import c.cache.VideoCache;
import c.report.Report;
import c.utils.Pool;
import c.wapper.ElementWrapper;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * Created on 06/27 2021.
 *
 * @author Bennie
 */
@Slf4j
public class Downloader implements Runnable {

    private static final Path DOWNLOAD_DIR;

    private static final String COMMAND = "export https_proxy=http://127.0.0.1:7890 http_proxy=http://127.0.0.1:7890 all_proxy=socks5://127.0.0.1:7890 && ffmpeg -y -i '%s' -acodec copy -vcodec copy '%s'";

    private static final String MP4 = ".mp4";

    static {
        DOWNLOAD_DIR = Paths.get(Config.getDownloadDir(), LocalDate.now().toString());
    }

    private final BlockingQueue<ElementWrapper> queue;

    public Downloader(BlockingQueue<ElementWrapper> queue) {
        this.queue = queue;
    }

    private int outputToMp4(String title, String m3u8Url) throws InterruptedException, IOException {
        Path directories = Files.createDirectories(DOWNLOAD_DIR).resolve(title + MP4);

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
        ElementWrapper element = null;
        try {
            while (true) {
                element = queue.poll(30, TimeUnit.SECONDS);
                if (element == null) {
                    break;
                }
                // 标题
                String title = element.getTitle();
                log.debug("get: {}", title);

                // 存在的话获取路径
                String path = VideoCache.get(title);

                if (Objects.nonNull(path)) {
                    Report.downSkip(element.getSourceUrl());
                    log.debug("跳过: {}, {}", title, path);
                    continue;
                }
                String url      = element.getUrl();
                Double duration = element.getDuration();
                if (duration == null) {
                    HtmlCache.delete(url);
                    continue;
                }

                long start   = System.currentTimeMillis();
                long timeout = element.timeout();
                log.info("开始下载: [超时: {} / 总时长: {}], 名称: [{}], 来源: [{}], 地址: [{}]", timeout, duration, title, element.getSourceUrl(), url);

                String              m3u8Src           = element.getRealUrl();
                FutureTask<Integer> integerFutureTask = new FutureTask<>(() -> outputToMp4(title, m3u8Src));
                Thread              thread            = new Thread(integerFutureTask, "down-");
                thread.start();

                try {
                    Integer i = integerFutureTask.get(element.timeout(), TimeUnit.MINUTES);
                    if (i == 0) {
                        log.info("下载完成: 耗时: [{}], 名称: [{}], 地址: [{}]", watch(start), title, url);
                        Report.downSuccess(element.getSourceUrl());
                    } else {
                        log.error("下载失败: 耗时: [{}], 名称 [{}], 地址: [{}]", watch(start), title, url);
                        Report.downFail(element.getSourceUrl());
                        VideoCache.delete(title);
                    }
                    HtmlCache.delete(url);
                } catch (ExecutionException e) {
                    log.error("下载失败, 再次放入队列", e);
                    queue.add(element);
                } catch (TimeoutException e) {
                    Report.downTimeout(element.getSourceUrl());
                    VideoCache.delete(title);
                    HtmlCache.delete(url);
                    log.error("下载超时删除: 耗时: [{}], 名称: [{}], 地址: [{}]", watch(start), title, url);
                }
            }
        } catch (IOException e) {
            log.error("下载失败, 再次放入队列", e);
            queue.add(element);
        } catch (InterruptedException e) {
            // ignore
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        log.info("Download -> 退出");
        Pool.finish();
    }

    private String watch(long start) {
        double d = ((System.currentTimeMillis() - start) / 1000d) / 60d;
        return String.format("%.2f", d);

    }

}
