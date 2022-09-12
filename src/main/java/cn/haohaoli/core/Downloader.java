package cn.haohaoli.core;

import cn.haohaoli.event.*;
import cn.haohaoli.component.EventPublisher;
import cn.haohaoli.filter.Filter;
import cn.haohaoli.utils.ProcessUtils;
import cn.haohaoli.wapper.ElementWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.concurrent.*;

/**
 * @author lwh
 */
@Slf4j
@AllArgsConstructor
public class Downloader implements Runnable {

    private final BlockingQueue<ElementWrapper> queue;

    private final Filter filter;

    @Override
    public void run() {
        log.info("开始");
        try {
            while (true) {
                try (ElementWrapper element = queue.poll(30, TimeUnit.SECONDS)) {
                    if (element == null) {
                        return;
                    }
                    if (!filter.apply(element)) {
                        continue;
                    }
                    download(element);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            log.info("Download -> 退出");
            EventPublisher.publish(new DownloaderFinishEvent());
        }
    }

    private void download(ElementWrapper element) {
        long start = System.currentTimeMillis();

        String title   = element.getTitle();
        String url     = element.getUrl();
        int   timeout = (int) element.timeout();

        log.info("开始下载: [超时: {}], 名称: [{}], 来源: [{}], 地址: [{}]", timeout, title, element.getSourceUrl(), url);

        try {
            ProcessUtils.Result result = ProcessUtils.outputToMp4(timeout, element.downDir(), element.getFieldName(), element.getRealUrl());
            if (result.getCode() == 0) {
                log.info("下载完成: 耗时: [{}], 名称: [{}], 地址: [{}]", watch(start), title, url);
                EventPublisher.publish(new VideoDownSuccessEvent(element));
            } else {
                log.error("下载失败: 耗时: [{}], 名称: [{}], 信息: [{}], 地址: [{}]", watch(start), title, result.getMsg(), url);
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

    /**
     * 转分钟
     *
     * @param start
     * @return
     */
    private String watch(long start) {
        double d = ((System.currentTimeMillis() - start) / 1000d) / 60d;
        return String.format("%.2f", d);
    }

}
