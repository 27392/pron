package cn.haohaoli.filter;

import cn.haohaoli.cache.VideoCache;
import cn.haohaoli.component.EventPublisher;
import cn.haohaoli.event.VideoSkipEvent;
import cn.haohaoli.wapper.ElementWrapper;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lwh
 */
@Slf4j
public class CacheExistFilter implements Filter {

    @Override
    public boolean apply(ElementWrapper wrapper) {
        String title = wrapper.getTitle();
        if (wrapper.exist()) {
            log.info("存在跳过: {}, {}", title, VideoCache.get(wrapper.getId()));
            EventPublisher.publish(new VideoSkipEvent(wrapper));
            return false;
        }
        return true;
    }

    @Override
    public int order() {
        return 0;
    }
}