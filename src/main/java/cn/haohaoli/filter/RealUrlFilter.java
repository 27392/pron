package cn.haohaoli.filter;

import cn.haohaoli.component.EventPublisher;
import cn.haohaoli.event.VideoDownFailEvent;
import cn.haohaoli.event.VideoSkipEvent;
import cn.haohaoli.wapper.ElementWrapper;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lwh
 */
@Slf4j
public class RealUrlFilter implements Filter {

    @Override
    public boolean apply(ElementWrapper wrapper) throws Exception {
        String title   = wrapper.getTitle();
        String m3u8Src = wrapper.getRealUrl();
        if (m3u8Src == null) {
            log.info("地址错误: {}", title);
            EventPublisher.publish(new VideoDownFailEvent(wrapper));
            return false;
        }
        return true;
    }

    @Override
    public int order() {
        return 3;
    }
}