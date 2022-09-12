package cn.haohaoli;

import cn.haohaoli.config.Config;
import cn.haohaoli.core.TypeEnum;
import cn.haohaoli.listener.*;
import cn.haohaoli.component.EventPublisher;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * @author lwh
 */
@Slf4j
public class Main {

    static {
        EventPublisher.register(new ReportListener());
        EventPublisher.register(new VideoListener());
        EventPublisher.register(new TaskListener());
        EventPublisher.register(new BeyondListener());
        EventPublisher.register(new HtmlListener());

        try {
            Class.forName("cn.haohaoli.config.Config");
            Class.forName("cn.haohaoli.cache.HtmlCache");
            Class.forName("cn.haohaoli.cache.VideoCache");
            Class.forName("cn.haohaoli.beyond.Beyond");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        log.info("下载发布时间在[{} - {}]之前的{}页{}分钟的视频", Config.getLastTime(), LocalDate.now(), Config.getMaxPage(), Config.getMaxDuration());
    }

    public static void main(String[] args) throws Exception {
        TypeEnum type = Config.getType();
        log.info("{}", type);

        type.start(new ArrayBlockingQueue<>(((4 * 6) * 3)), Config.getDownCount());
    }

}
