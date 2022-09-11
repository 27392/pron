package cn.haohaoli.listener;

import cn.haohaoli.event.*;
import cn.haohaoli.report.Report;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lwh
 */
@Slf4j
@SuppressWarnings("all")
public class ReportListener {

    @Subscribe
    public void skip(VideoSkipEvent event) {
        String sourceUrl = event.getWrapper().getSourceUrl();
        Report.downSkip(sourceUrl);
    }

    @Subscribe
    public void expired(VideoExpiredEvent event) {
        String sourceUrl = event.getWrapper().getSourceUrl();
        Report.downExpired(sourceUrl);
    }

    @Subscribe
    public void downSuccess(VideoDownSuccessEvent event) {
        String sourceUrl = event.getWrapper().getSourceUrl();
        Report.downSuccess(sourceUrl);
    }

    @Subscribe
    public void downFail(VideoDownFailEvent event) {
        String sourceUrl = event.getWrapper().getSourceUrl();
        Report.downFail(sourceUrl);
    }

    @Subscribe
    public void downTimeout(VideoDownTimeoutEvent event) {
        String sourceUrl = event.getWrapper().getSourceUrl();
        Report.downTimeout(sourceUrl);
    }

    @Subscribe
    public void durationLong(VideoDurationLongEvent event) {
        String sourceUrl = event.getWrapper().getSourceUrl();
        Report.downTimeBeyond(sourceUrl);
    }

    @Subscribe
    public void httpSuccess(HttpSuccessEvent event) {
        Report.httpRequest();
    }

    @Subscribe
    public void pagePut(PagePutEvent event) {
        String sourceUrl = event.getWrapper().getSourceUrl();
        Report.produce(sourceUrl);
    }
}
