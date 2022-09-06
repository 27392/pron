package cn.haohaoli.listener;

import c.event.*;
import cn.haohaoli.event.*;
import cn.haohaoli.report.Report;
import com.google.common.eventbus.Subscribe;
import com.haohaoli.event.*;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lwh
 */
@Slf4j
@SuppressWarnings("all")
public class ReportListener {

    @Subscribe
    public void skip(VideoSkipEvent event) {
        String title     = event.getWrapper().getTitle();
        String sourceUrl = event.getWrapper().getSourceUrl();
        Report.downSkip(sourceUrl);
    }

    @Subscribe
    public void expired(VideoExpiredEvent event) {
        String title     = event.getWrapper().getTitle();
        String sourceUrl = event.getWrapper().getSourceUrl();
        Report.downExpired(sourceUrl);
    }

    @Subscribe
    public void downSuccess(VideoDownSuccessEvent event) {
        String title     = event.getWrapper().getTitle();
        String sourceUrl = event.getWrapper().getSourceUrl();
        Report.downSuccess(sourceUrl);
    }

    @Subscribe
    public void downFail(VideoDownFailEvent event) {
        String title     = event.getWrapper().getTitle();
        String sourceUrl = event.getWrapper().getSourceUrl();
        Report.downFail(sourceUrl);
    }

    @Subscribe
    public void downTimeout(VideoDownTimeoutEvent event) {
        String title     = event.getWrapper().getTitle();
        String sourceUrl = event.getWrapper().getSourceUrl();
        Report.downTimeout(sourceUrl);
    }

    @Subscribe
    public void httpSuccess(HttpSuccessEvent event) {
        Report.httpRequest();
    }
}
