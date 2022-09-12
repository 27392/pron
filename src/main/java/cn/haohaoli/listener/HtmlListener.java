package cn.haohaoli.listener;

import cn.haohaoli.cache.HtmlCache;
import cn.haohaoli.cache.VideoCache;
import cn.haohaoli.event.VideoDownFailEvent;
import cn.haohaoli.event.VideoDownSuccessEvent;
import cn.haohaoli.event.VideoDownTimeoutEvent;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDate;

/**
 * @author lwh
 */
@Slf4j
@SuppressWarnings("all")
public class HtmlListener {

    @Subscribe
    public void downSuccess(VideoDownSuccessEvent event) {
        String  url    = event.getWrapper().getUrl();
        boolean delete = HtmlCache.delete(url);
        log.info("删除缓存: {}", delete);
    }
}
