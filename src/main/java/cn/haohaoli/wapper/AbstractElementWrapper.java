package cn.haohaoli.wapper;

import cn.haohaoli.config.Config;
import cn.haohaoli.beyond.Beyond;
import cn.haohaoli.cache.VideoCache;
import cn.haohaoli.utils.ProcessUtils;
import cn.haohaoli.beyond.Entry;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * @author lwh
 */
@Slf4j
public abstract class AbstractElementWrapper implements ElementWrapper {

    @Override
    public double getDuration() throws Exception {
        String url   = this.getUrl();
        Entry  entry = Beyond.get(url);

        if (entry != null) {
            return entry.getTime();
        }
        String realUrl = this.getRealUrl();
        return duration(realUrl);
    }

    @Override
    public long timeout() {
        return Config.getDownloadTimeout();
    }

    @Override
    public boolean exist() {
        String title = getTitle();
        String path  = VideoCache.get(title);
        return path != null;
    }

    protected double duration(String m3u8Url) throws IOException, InterruptedException {
        return ProcessUtils.duration(m3u8Url);
    }
}
