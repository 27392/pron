package cn.haohaoli.utils;

import cn.haohaoli.config.Config;
import cn.haohaoli.cache.HtmlCache;
import cn.haohaoli.component.EventPublisher;
import cn.haohaoli.event.HttpSuccessEvent;
import cn.haohaoli.wapper.DocumentWrapper;
import cn.haohaoli.report.Report;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static cn.haohaoli.cmmon.Const.UA;

/**
 * @author lwh
 */
@Slf4j
public class HttpHelper {

    private static final Proxy DEFAULT_PROXY;

    private static final HashMap<String, List<Cookie>> COOKIE_STORE = new HashMap<>();

    static {
        try {
            String[] split = Config.getProxy().split(":");
            DEFAULT_PROXY = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(split[0], Integer.parseInt(split[1])));
        } catch (Exception e) {
            log.error("代理配置错误", e);
            throw new RuntimeException(e);
        }
    }

    public static String randomIp() {
        return new Random()
                .ints(10, 255)
                .limit(4)
                .mapToObj(Objects::toString)
                .collect(Collectors.joining("."));
    }

    public static DocumentWrapper http(String url) throws IOException, InterruptedException {
        Throwable       ex      = null;
        DocumentWrapper wrapper = null;
        for (int i = 0; i < 5; i++) {
            try {
                wrapper = httpForCache(url);
                break;
            } catch (Exception e) {
                ex = e;
                log.info("重试: [{}], [第{}次], [{}]", url, i, e.getMessage());
            }
        }
        if (wrapper != null) {
            if (wrapper.getType() == DocumentWrapper.Type.REMOTE) {
                EventPublisher.publish(new HttpSuccessEvent(wrapper));
            }
            return wrapper;
        }
        throw new RuntimeException(ex);
    }

    public static DocumentWrapper httpForCache(String url) throws IOException, InterruptedException {
        // 检查缓存
        String html = HtmlCache.get(url);
        if (Objects.nonNull(html) && !"".equals(html)) {
            log.debug("get for cache: {}", url);
            return DocumentWrapper.of(Jsoup.parse(html), DocumentWrapper.Type.CACHE);
        }
        // 发起请求
        DocumentWrapper documentWrapper = doHttp(url);

        // 缓存
        try {
            HtmlCache.save(url, documentWrapper.getDocument().html());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return documentWrapper;
    }

    public static DocumentWrapper doHttp(String url) throws IOException, InterruptedException {
        Report.httpRequest();
        log.debug("get for http: {}", url);

        TimeUnit.SECONDS.sleep(3);

        Request req = new Request.Builder()
                .url(url)
                .get()
                .header("X-Forwarded-For", randomIp())
                .header("Accept-Language", "zh-CN,zh")
                .header("User-Agent", UA)
                .build();
        Response response = OkHttpClientHolder.INSTANCE.newCall(req).execute();

        if (response.isSuccessful()) {
            ResponseBody body     = response.body();
            String       text     = new String(body.bytes());
            Document     document = Jsoup.parse(text);
            return DocumentWrapper.of(document, DocumentWrapper.Type.REMOTE);
        } else {
            throw new RuntimeException("url: " + url + ", code: " + response.code());
        }
    }

    private static class OkHttpClientHolder {

        static OkHttpClient INSTANCE = create();

        private static OkHttpClient create() {
            return new OkHttpClient()
                    .newBuilder()
                    .connectionPool(new ConnectionPool(20, 5, TimeUnit.SECONDS))
                    .proxy(DEFAULT_PROXY)
                    .cookieJar(new CookieJar() {
                        @Override
                        public void saveFromResponse(@NotNull HttpUrl httpUrl, @NotNull List<Cookie> list) {
                            COOKIE_STORE.put(httpUrl.host(), list);
                        }

                        @NotNull
                        @Override
                        public List<Cookie> loadForRequest(@NotNull HttpUrl httpUrl) {
                            return COOKIE_STORE.getOrDefault(httpUrl.host(), new ArrayList<>());
                        }
                    })
                    .callTimeout(Duration.ofSeconds(5))
                    .build();
        }
    }
}
