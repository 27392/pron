package c.listener;

import c.event.DownloaderFinishEvent;
import c.event.PageResolveFinishEvent;
import c.utils.TaskUtils;
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
}
