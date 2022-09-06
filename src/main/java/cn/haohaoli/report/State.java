package cn.haohaoli.report;

import lombok.Getter;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author lwh
 */
@Getter
public class State {

    private final AtomicInteger produce = new AtomicInteger();

    private final AtomicInteger downSkip       = new AtomicInteger();
    private final AtomicInteger downTimeout    = new AtomicInteger();
    private final AtomicInteger downTimeBeyond = new AtomicInteger();
    private final AtomicInteger downSuccess    = new AtomicInteger();
    private final AtomicInteger downFail       = new AtomicInteger();
    private final AtomicInteger downExpired    = new AtomicInteger();


    public int produceCount() {
        return produce.get();
    }

    public int consumeCount() {
        return downSkip.get() +
                downTimeout.get() +
                downTimeBeyond.get() +
                downSuccess.get() +
                downFail.get() +
                downExpired.get();
    }

    @Override
    public String toString() {
        return "共获取=" + produce + ", [成功=" + downSuccess + ", 失败=" + downFail + ", 过期=" + downExpired + ", 跳过=" + downSkip + ", 超时=" + downTimeout + ", 时长过长=" + downTimeBeyond + "]";
    }
}