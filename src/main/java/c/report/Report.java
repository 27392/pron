package c.report;

import c.utils.Pool;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author lwh
 */
@Slf4j
public class Report {

    private static final LocalDateTime now       = LocalDateTime.now();
    private static final AtomicInteger httpCount = new AtomicInteger();

    private static final Map<String, State> report = Collections.synchronizedMap(new StateMap());

    static {
        final Set<String> finish = new HashSet<>();
        final String      end    = "&page=";
        Pool.scheduleAtFixedRate(() -> {
            HashSet<String> w = new LinkedHashSet<>();
            report.forEach((k, v) -> {
                int computed = computed(k, finish);
                if (computed != 0 && !finish.contains(k)) {
                    w.add(k.substring(k.lastIndexOf(end) + end.length()) + "页剩余" + computed);
                }
            });
            if (!w.isEmpty()) {
                log.info("[{}]", String.join("], [", w));
            }
        }, 10, 30, TimeUnit.SECONDS);
    }

    static class StateMap extends LinkedHashMap<String, State> {
        @Override
        public State put(String key, State value) {
            State o = get(key);
            if (o == null) {
                o = new State();
            }
            o.getProduce().incrementAndGet();
            return super.put(key, o);
        }

        @Override
        public State get(Object key) {
            if (super.get(key) == null) {
                super.put((String) key, new State());
            }
            return super.get(key);
        }
    }

    public static void produce(String url) {
        report.put(url, null);
    }

    public static void downSkip(String url) {
        report.get(url).getDownSkip().incrementAndGet();
    }

    public static void downTimeout(String url) {
        report.get(url).getDownTimeout().incrementAndGet();
    }

    public static void downTimeBeyond(String url) {
        report.get(url).getDownTimeBeyond().incrementAndGet();
    }

    public static void downSuccess(String url) {
        report.get(url).getDownSuccess().incrementAndGet();
    }

    public static void downFail(String url) {
        report.get(url).getDownFail().incrementAndGet();
    }

    public static void httpRequest() {
        httpCount.incrementAndGet();
    }

    private static int computed(String url, Set<String> finish) {
        State state = report.get(url);
        int   p     = state.getProduce().get();
        int c = state.getDownSuccess().get() +
                state.getDownFail().get() +
                state.getDownTimeout().get() +
                state.getDownTimeBeyond().get() +
                state.getDownSkip().get();
        if (p == c && !finish.contains(url)) {
            log.info("{} 处理完成, 状态: {}", url, state);
            finish.add(url);
            return 0;
        }
        return p - c;
    }

    public static String print() {
        LinkedHashMap<String, State> map = new LinkedHashMap<>();
        report.forEach((k, v) -> {
            String substring = k.substring(0, k.lastIndexOf("&page="));

            State state = map.get(substring);
            if (state == null) {
                map.put(substring, v);
            } else {
                state.getProduce().addAndGet(v.getProduce().get());
                state.getDownFail().addAndGet(v.getDownFail().get());
                state.getDownSuccess().addAndGet(v.getDownSuccess().get());
                state.getDownSkip().addAndGet(v.getDownSkip().get());
                state.getDownTimeout().addAndGet(v.getDownTimeout().get());
                state.getDownTimeBeyond().addAndGet(v.getDownTimeBeyond().get());
            }
        });
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n")
                .append("网络请数量: ").append(httpCount.get())
                .append(", 耗时: ").append(now).append(", 结束 ").append(LocalDateTime.now());
        map.forEach((k, v) -> {
            stringBuilder.append("\n")
                    .append("url ").append("[").append(k).append("]")
                    .append(", ")
                    .append(v.toString());
        });
        return stringBuilder.toString();
    }

}
