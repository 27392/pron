package cn.haohaoli.listener;

import cn.haohaoli.beyond.Beyond;
import cn.haohaoli.beyond.Entry;
import cn.haohaoli.event.CloseEvent;
import cn.haohaoli.event.VideoDownSuccessEvent;
import cn.haohaoli.event.VideoDurationLongEvent;
import cn.haohaoli.utils.TaskUtils;
import cn.haohaoli.wapper.ElementWrapper;
import com.google.common.eventbus.Subscribe;

import java.io.IOException;
import java.time.LocalDate;

/**
 * @author lwh
 */
@SuppressWarnings("all")
public class BeyondListener {

    @Subscribe
    public void durationLongEvent(VideoDurationLongEvent event) throws Exception {
        ElementWrapper wrapper     = event.getWrapper();
        String         realUrl     = wrapper.getRealUrl();
        LocalDate      releaseDate = wrapper.getReleaseDate();
        double         duration    = wrapper.getDuration();
        String         url         = wrapper.getUrl();
        String         title       = wrapper.getTitle();
        Beyond.add(new Entry(title, releaseDate, duration, url, realUrl));
    }

    @Subscribe
    public void downSuccessEvent(VideoDownSuccessEvent event) throws Exception {
        Beyond.remove(event.getWrapper().getUrl());
    }

    @Subscribe
    public void close(CloseEvent event) throws IOException {
        Beyond.refresh();
    }

}
