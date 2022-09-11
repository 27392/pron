package cn.haohaoli.filter;

import cn.haohaoli.component.EventPublisher;
import cn.haohaoli.config.Config;
import cn.haohaoli.core.TypeEnum;
import cn.haohaoli.event.VideoDurationLongEvent;
import cn.haohaoli.wapper.ElementWrapper;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lwh
 */
@Slf4j
public class DurationLongFilter implements Filter {

    @Override
    public boolean apply(ElementWrapper wrapper) throws Exception {
        String title    = wrapper.getTitle();
        double duration = wrapper.getDuration();
        if (Config.getType() == TypeEnum.BEYOND) {
            if (duration > Config.getBeyondMaxDuration()) {
                log.info("时间超长: {}", title);
                EventPublisher.publish(new VideoDurationLongEvent(wrapper));
                return false;
            }
        } else {
            if (duration > Config.getMaxDuration()) {
                log.info("时间超长: {}", title);
                EventPublisher.publish(new VideoDurationLongEvent(wrapper));
                return false;
            }
        }
        return true;
    }

    @Override
    public int order() {
        return 1;
    }
}