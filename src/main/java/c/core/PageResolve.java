package c.core;

import c.Config;
import c.utils.Pool;
import c.report.Report;
import c.utils.HttpHelper;
import c.wapper.DocumentWrapper;
import c.wapper.DefaultElementWrapper;
import c.wapper.ElementWrapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
@Slf4j
public class PageResolve implements Runnable {

    private BlockingQueue<ElementWrapper> queue;

    private TypeEnum type;

    private String uid;

    private Integer maxPage;

    private AtomicInteger currentPage;

    private Document document;

    private String currentUrl;

    private boolean refreshMaxPage;

    public PageResolve(BlockingQueue<ElementWrapper> queue, TypeEnum type) {
        this(queue, type, null);
    }

    public PageResolve(BlockingQueue<ElementWrapper> queue, TypeEnum type, String uid) {
        this.type = type;
        this.maxPage = 1;
        this.currentPage = new AtomicInteger(0);
        this.refreshMaxPage = false;
        this.queue = queue;
        this.uid = uid;
    }

    public Document nextPage() {
        if (currentPage.get() == maxPage) {
            this.refreshMaxPage = false;
            refreshPageNum();
            if (currentPage.get() >= maxPage) {
                log.info("{}, 最后一页", currentUrl);
                return null;
            }
        }
        Document document;
        try {
            if (type == TypeEnum.UID) {
                currentUrl = String.format(type.getUrl(), uid) + "&page=" + currentPage.incrementAndGet();
            } else {
                currentUrl = type.getUrl() + "&page=" + currentPage.incrementAndGet();
            }
            DocumentWrapper documentWrapper = HttpHelper.doHttp(currentUrl);
            if (documentWrapper.getType() == DocumentWrapper.Type.REMOTE && currentPage.get() != 1) {
                TimeUnit.SECONDS.sleep(20);
            }
            document = documentWrapper.getDocument();
        } catch (IOException e) {
            log.error("获取下一页失败", e);
            return null;
        } catch (InterruptedException e) {
            return null;
        }
        this.document = document;
        refreshPageNum();
        return document;
    }

    private void refreshPageNum() {
        // 刷新页码
        if (!refreshMaxPage) {
            int configMaxPageSiz = Config.getMaxPage();
            int maxPageSize      = type.getMaxPageSize(document);
            if (maxPageSize > configMaxPageSiz) {
                log.warn("页码大于配置最大页码数. 页码: {}, 配置页码: {}", maxPageSize, configMaxPageSiz);
                this.maxPage = configMaxPageSiz;
            } else {
                this.maxPage = maxPageSize;
            }
            if (currentPage.get() > 1) {
                log.info("{}, 刷新总页数: {}", currentUrl, this.maxPage);
            } else {
                log.info("{}, 总页数: {}", currentUrl, this.maxPage);
            }
            // 刷新标识
            this.refreshMaxPage = true;
        }
    }

    @Override
    public void run() {
        try {
            Document doc;
            while ((doc = nextPage()) != null) {
                log.info("{}, 当前页: {}", currentUrl, currentPage);

                Elements select = doc.select("#wrapper > div.container > .row > div > .row > div");
                for (Element element : select) {
                    queue.put(new DefaultElementWrapper(element, currentUrl));
                    Report.produce(currentUrl);

                    String href = element.select(".videos-text-align > a").attr("href");
                    if ("".equals(href)) {
                        href = element.select(".well > a").attr("href");
                    }
                    String title = element.select(".video-title").html().replace("[原创]", "").trim();
                    log.debug("put: {}, {}, {}", currentPage, title, href);
                }
                log.info("{}, 第{}页, 完成", currentUrl, currentPage);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        log.info("退出");
        Pool.finish();
    }
}