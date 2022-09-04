package c.report;

import lombok.Getter;

import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class State {
    private final AtomicInteger produce = new AtomicInteger();

    private final AtomicInteger downSkip = new AtomicInteger();
    private final AtomicInteger downTimeout = new AtomicInteger();
    private final AtomicInteger downTimeBeyond = new AtomicInteger();
    private final AtomicInteger downSuccess = new AtomicInteger();
    private final AtomicInteger downFail = new AtomicInteger();

    @Override
    public String toString() {
        return "生产=" + produce + ",成功= " + downSuccess + ", 失败= " + downFail + ", 跳过=" + downSkip + ", 超时=" + downTimeout + ", 时长过长=" + downTimeBeyond;
    }
}