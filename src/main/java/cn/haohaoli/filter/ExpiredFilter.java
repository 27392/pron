package cn.haohaoli.filter;

import cn.haohaoli.component.EventPublisher;
import cn.haohaoli.config.Config;
import cn.haohaoli.event.VideoExpiredEvent;
import cn.haohaoli.wapper.ElementWrapper;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

/**
 * @author lwh
 */
@Slf4j
public class ExpiredFilter implements Filter {

    @Override
    public boolean apply(ElementWrapper wrapper) {
        String    title       = wrapper.getTitle();
        String    url         = wrapper.getUrl();
        LocalDate releaseDate = wrapper.getReleaseDate();
        if (Config.getLastTime().compareTo(releaseDate) > 0) {
            log.info("时间过期: {}, 来源: [{}], 地址: [{}]", title, wrapper.getSourceUrl(), url);
            EventPublisher.publish(new VideoExpiredEvent(wrapper));
            return false;
        }
        return true;
    }

    @Override
    public int order() {
        return 2;
    }
}