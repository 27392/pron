package cn.haohaoli.wapper;

import cn.haohaoli.config.Config;
import cn.haohaoli.beyond.Beyond;
import cn.haohaoli.cache.VideoCache;
import cn.haohaoli.utils.ProcessUtils;
import cn.haohaoli.beyond.Entry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import java.io.IOException;

/**
 * @author lwh
 */
@Slf4j
public abstract class AbstractElementWrapper implements ElementWrapper {

    private final String id;

    public AbstractElementWrapper(String id) {
        this.id = id;
        MDC.put("id", StringUtils.rightPad(id, 20));
    }

    @Override
    public String getId() {
        return id;
    }

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
        String path = VideoCache.get(getId());
        return path != null;
    }

    protected double duration(String m3u8Url) throws IOException, InterruptedException {
        return ProcessUtils.duration(m3u8Url);
    }

    @Override
    public void close() throws IOException {
        MDC.clear();
    }
}
