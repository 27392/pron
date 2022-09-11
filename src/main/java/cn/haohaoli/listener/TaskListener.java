package cn.haohaoli.listener;

import cn.haohaoli.event.CloseEvent;
import cn.haohaoli.event.PageResolveFinishEvent;
import cn.haohaoli.utils.TaskUtils;
import cn.haohaoli.event.DownloaderFinishEvent;
import com.google.common.eventbus.Subscribe;

/**
 * @author lwh
 */
@SuppressWarnings("all")
public class TaskListener {

    @Subscribe
    public void downloaderFinish(DownloaderFinishEvent event) {
        TaskUtils.finish();
    }

    @Subscribe
    public void pageResolveFinish(PageResolveFinishEvent event) {
        TaskUtils.finish();
    }

    @Subscribe
    public void close(CloseEvent event) {
        TaskUtils.shutdown();
    }
}
