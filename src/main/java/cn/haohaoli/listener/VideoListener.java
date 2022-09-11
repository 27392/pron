package cn.haohaoli.listener;

import cn.haohaoli.cache.VideoCache;
import com.google.common.eventbus.Subscribe;
import cn.haohaoli.event.VideoDownFailEvent;
import cn.haohaoli.event.VideoDownTimeoutEvent;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDate;

/**
 * @author lwh
 */
@Slf4j
@SuppressWarnings("all")
public class VideoListener {

    @Subscribe
    public void downFail(VideoDownFailEvent event) {
        String    fileName    = event.getWrapper().getFieldName();
        LocalDate releaseDate = event.getWrapper().getReleaseDate();
        try {
            boolean delete = VideoCache.delete(releaseDate, fileName);
            log.info("删除视频: {}, {}", fileName, delete);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void downTimeout(VideoDownTimeoutEvent event) {
        String    fileName    = event.getWrapper().getFieldName();
        LocalDate releaseDate = event.getWrapper().getReleaseDate();
        try {
            boolean delete = VideoCache.delete(releaseDate, fileName);
            log.info("删除视频: {}, {}", fileName, delete);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
