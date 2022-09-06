package c.listener;

import c.beyond.Beyond;
import c.event.DownloaderDurationLongEvent;
import c.wapper.ElementWrapper;
import com.google.common.eventbus.Subscribe;

import java.time.LocalDate;

/**
 * @author lwh
 */
@SuppressWarnings("all")
public class BeyondListener {

    @Subscribe
    public void downloaderFinish(DownloaderDurationLongEvent event) throws Exception {
        ElementWrapper wrapper     = event.getWrapper();
        String         realUrl     = wrapper.getRealUrl();
        LocalDate      releaseDate = wrapper.getReleaseDate();
        double         duration    = wrapper.getDuration();
        String         url         = wrapper.getUrl();
        String         title       = wrapper.getTitle();
        Beyond.add(releaseDate, duration, url, realUrl, title);
    }

}
