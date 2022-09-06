package c.listen;

import c.cache.VideoCache;
import c.event.*;
import c.report.Report;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * @author lwh
 */
@Slf4j
@SuppressWarnings("all")
public class VideoListener {

    @Subscribe
    public void downFail (VideoDownFailEvent event) {
        String  title  = event.getWrapper().getTitle();
        try {
            boolean delete = VideoCache.delete(title);
            log.info("删除视频: {}, {}", title, delete);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void downTimeout (VideoDownTimeoutEvent event) {
        String  title  = event.getWrapper().getTitle();
        try {
            boolean delete = VideoCache.delete(title);
            log.info("删除视频: {}, {}", title, delete);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
