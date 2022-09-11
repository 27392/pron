package cn.haohaoli.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author lwh
 */
@Slf4j
public class TaskUtils {

    private static final ThreadPoolExecutor          threadPoolExecutor;
    private static final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

    public static final  AtomicInteger finishCount = new AtomicInteger();
    private static final int           cpuCores;

    static {
        AtomicInteger counter = new AtomicInteger();
        cpuCores = Runtime.getRuntime().availableProcessors();

        threadPoolExecutor = new ThreadPoolExecutor(
                0,
                cpuCores * 3,
                10, // 比下载超时时间大
                TimeUnit.MINUTES,
                new SynchronousQueue<>(),
                new ThreadFactory() {
                    @Override
                    public Thread newThread(@NotNull Runnable r) {
                        Thread thread = new Thread(r);
                        thread.setName("91-" + StringUtils.rightPad(Objects.toString(counter.incrementAndGet()), 2));
                        thread.setDaemon(false);
                        thread.setUncaughtExceptionHandler((t, e) -> log.error(e.getMessage(), e));
                        return thread;
                    }
                }, new ThreadPoolExecutor.AbortPolicy());

        scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
    }

    public static void submit(Runnable runnable) {
        threadPoolExecutor.submit(runnable);
    }

    public static int getCpuCores() {
        return cpuCores;
    }

    public static void shutdown() {
        threadPoolExecutor.shutdown();
        scheduledThreadPoolExecutor.shutdown();
    }

    public static void finish() {
        finishCount.incrementAndGet();
    }

    public static long getTimeOut() {
        return threadPoolExecutor.getKeepAliveTime(TimeUnit.MINUTES);
    }

    public static void submitSchedule(Runnable command, long initialDelay, long period, TimeUnit unit) {
        scheduledThreadPoolExecutor.scheduleAtFixedRate(command, initialDelay, period, unit);
    }
}
