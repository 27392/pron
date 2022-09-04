package c.wapper;

import c.Config;
import c.beyond.Beyond;
import c.beyond.Entry;
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

    private static final int MAX_DURATION = Config.getMaxDuration();
    private static final int TIMEOUT      = Config.getDownloadTimeout();

    private static final String DURATION = "export https_proxy=http://127.0.0.1:7890 http_proxy=http://127.0.0.1:7890 all_proxy=socks5://127.0.0.1:7890 && ffprobe -i '%s' -show_entries format=duration -v quiet -of csv='p=0'";

    @Override
    public Double getDuration() throws Exception {
        String realUrl   = this.getRealUrl();
        String sourceUrl = this.getSourceUrl();
        String title     = this.getTitle();
        String url       = this.getUrl();
        Entry  entry     = Beyond.get(realUrl);

        if (entry != null && entry.getTime() > MAX_DURATION) {
            Report.downTimeBeyond(this.getSourceUrl());
            log.debug("缓存中获取时间超长 {} 分钟: [{}]", entry.getTime(), title);
            return null;
        }
        double duration = duration(realUrl);
        if (duration > MAX_DURATION) {
            Report.downTimeBeyond(sourceUrl);
            log.debug("时间超长 {} 分钟: [{}]", duration, title);
            Beyond.add(duration, url, realUrl, title);
            return null;
        }
        return duration;
    }

    @Override
    public long timeout() {
        return TIMEOUT;
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
