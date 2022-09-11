package cn.haohaoli.core;

import cn.haohaoli.config.Config;
import cn.haohaoli.event.PagePutEvent;
import cn.haohaoli.event.PageResolveFinishEvent;
import cn.haohaoli.component.EventPublisher;
import cn.haohaoli.proxy.ElementWrapperProxy;
import cn.haohaoli.utils.HttpHelper;
import cn.haohaoli.wapper.DocumentWrapper;
import cn.haohaoli.wapper.DefaultElementWrapper;
import cn.haohaoli.wapper.ElementWrapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static cn.haohaoli.cmmon.Const.PAGE;

/**
 * @author lwh
 */
@Getter
@Slf4j
public class PageResolve implements Runnable {

    private final BlockingQueue<ElementWrapper> queue;

    private final TypeEnum type;

    private final String uid;

    private Integer maxPage;

    private final AtomicInteger currentPage;

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

    @Override
    public void run() {
        log.info("开始");
        try {
            Document doc;
            while ((doc = nextPage()) != null) {
                log.info("{}, 当前页: {}", currentUrl, currentPage);
                Elements elements = getElements(doc);
                for (Element element : elements) {
                    try {
                        ElementWrapper wrapper = put(new DefaultElementWrapper(element, currentUrl));
                        log.info("put: 第{}页, {}", currentPage, wrapper.getTitle());

                        EventPublisher.publish(new PagePutEvent(wrapper));
                    } finally {
                        MDC.clear();
                    }
                }
                log.info("{}, 第{}页, 完成", currentUrl, currentPage);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        log.info("退出");
        EventPublisher.publish(new PageResolveFinishEvent());
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
                currentUrl = String.format(type.getUrl(), uid) + PAGE + currentPage.incrementAndGet();
            } else {
                currentUrl = type.getUrl() + PAGE + currentPage.incrementAndGet();
            }
            DocumentWrapper documentWrapper = HttpHelper.http(currentUrl);
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

    private ElementWrapper put(ElementWrapper wrapper) throws InterruptedException {
        ElementWrapper wrapperProxy = ElementWrapperProxy.create(wrapper);
        queue.put(wrapperProxy);
        return wrapperProxy;
    }

    private Elements getElements(Document doc) {
        return doc.select("#wrapper > div.container > .row > div > .row > div");
    }
}