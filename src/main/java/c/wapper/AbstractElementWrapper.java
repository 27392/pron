package c.wapper;

import c.Config;
import c.beyond.Beyond;
import c.beyond.Entry;
import c.cache.VideoCache;
import c.report.Report;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author lwh
 */
@Slf4j
public abstract class AbstractElementWrapper implements ElementWrapper {

    private static final String DURATION = "export https_proxy=http://127.0.0.1:7890 http_proxy=http://127.0.0.1:7890 all_proxy=socks5://127.0.0.1:7890 && ffprobe -i '%s' -show_entries format=duration -v quiet -of csv='p=0'";

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
        String         cmd     = String.format(DURATION, m3u8Url);
        ProcessBuilder builder = new ProcessBuilder("/bin/sh", "-c", cmd);
        Process        p       = builder.start();
        int            i       = p.waitFor();
        if (i != 0) {
            log.warn("获取时长错误: {}", cmd);
            return 1d;
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String str;
            while ((str = bufferedReader.readLine()) != null) {
                sb.append(str);
            }
        }
        return new BigDecimal(sb.toString()).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_EVEN).doubleValue();
    }
}
