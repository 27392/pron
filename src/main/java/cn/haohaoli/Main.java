package cn.haohaoli;

import cn.haohaoli.config.Config;
import cn.haohaoli.core.TypeEnum;
import cn.haohaoli.listener.BeyondListener;
import cn.haohaoli.listener.ReportListener;
import cn.haohaoli.listener.TaskListener;
import cn.haohaoli.listener.VideoListener;
import cn.haohaoli.component.EventPublisher;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URISyntaxException;
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
    }

    public static void main(String[] args) throws InterruptedException, URISyntaxException, IOException {
        TypeEnum type = Config.getType();
        log.info("{}", type);

        type.start(new ArrayBlockingQueue<>(((4 * 6) * 3)), Config.getDownCount());
    }

}
