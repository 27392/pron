package c.utils;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author lwh
 */
public class Pool {

    private static final ThreadPoolExecutor          threadPoolExecutor;
    private static final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

    public static final AtomicInteger finishCount = new AtomicInteger();

    private static final int cpuCores;

    static {
        cpuCores = Runtime.getRuntime().availableProcessors();
        AtomicInteger counter = new AtomicInteger();

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
                        thread.setName("91-" + counter.incrementAndGet());
                        thread.setDaemon(false);
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

    public static void scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        scheduledThreadPoolExecutor.scheduleAtFixedRate(command, initialDelay, period, unit);
    }
}
