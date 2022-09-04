package c.utils;

import c.report.Report;
import c.wapper.DocumentWrapper;
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

/**
 * @author lwh
 */
@Slf4j
public class HttpHelper {

    private static final Proxy DEFAULT_PROXY = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 7890));

    private static final HashMap<String, List<Cookie>> COOKIE_STORE = new HashMap<>();

    private static final String UA = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.101 Safari/537.36";

    public static String randomIp() {
        return new Random()
                .ints(10, 255)
                .limit(4)
                .mapToObj(Objects::toString)
                .collect(Collectors.joining("."));
    }

    public static DocumentWrapper doHttp(String url) throws IOException {
        String cacheKey = getCacheKey(url);
        for (int i = 0; i < 5; i++) {
            try {
                DocumentWrapper document = doHttp(url, DEFAULT_PROXY, Duration.ofSeconds(5));
                boolean equals = document.getDocument().select("title").text().equals("Confirm");
                if (equals) {
                    log.info("重试: {},{}", url, i);
                } else {
                    // 缓存
                    IOHelper.saveHtml(cacheKey, document.getDocument().html());
                    return document;
                }
            } catch (Exception e) {
                if (i == 4) {
                    throw e;
                }
                log.info("重试: {},{}", url, i);
            }
        }
        throw new RuntimeException("错误");
    }

    public static String getCacheKey(String url) {
        return url.replace("https://91porn.com/", "");
    }

    public static DocumentWrapper doHttp(String url, Proxy proxy, Duration timeout) throws IOException {
        String cacheKey = getCacheKey(url);

        // 检查缓存
        String html = IOHelper.readHtml(cacheKey);
        if (Objects.nonNull(html) && !"".equals(html)) {
            log.debug("get for cache: {}", url);
            return DocumentWrapper.of(Jsoup.parse(html), DocumentWrapper.Type.CACHE);
        }
        Report.httpRequest();
        log.debug("get for http: {}", url);

        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 发送请求
        OkHttpClient httpClient = new OkHttpClient()
                .newBuilder()
                .connectionPool(new ConnectionPool(20, 5, TimeUnit.SECONDS))
                .proxy(proxy)
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
                .callTimeout(timeout)
                .build();
        Request req = new Request.Builder()
                .url(url)
                .get()
                .header("X-Forwarded-For", randomIp())
                .header("Accept-Language", "zh-CN,zh")
                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36")
                .build();
        Response response = httpClient.newCall(req).execute();

        if (response.isSuccessful()) {
            ResponseBody body = response.body();
            String       text = new String(body.bytes());
            return DocumentWrapper.of(Jsoup.parse(text), DocumentWrapper.Type.REMOTE);
        } else {
            throw new RuntimeException("code: " + response.code());
        }
    }

}
