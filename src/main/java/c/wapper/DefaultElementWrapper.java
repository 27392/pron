package c.wapper;

import c.beyond.Beyond;
import c.core.Downloader;
import c.utils.HttpHelper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author lwh
 */
@Slf4j
@Getter
@AllArgsConstructor
public class DefaultElementWrapper extends AbstractElementWrapper {

    private static final Pattern ENCODE_STR_REGEX = Pattern.compile("strencode.+\\((.+?)\\)");
    private static final Pattern URL_REGEX        = Pattern.compile("src='((.+?)(\\.m3u8))'");
    private static final String  script;

    private final Element element;
    private final String  sourceUrl;

    static {
        try {
            Path path = Paths.get(Downloader.class.getClassLoader().getResource("m.js").toURI());
            script = Files.lines(path).collect(Collectors.joining());
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    static class ExecutorFactory {
        private static volatile JavascriptExecutor executor;

        static JavascriptExecutor getInstance() {
            if (executor == null) {
                synchronized (ExecutorFactory.class) {
                    if (executor == null) {
                        ChromeOptions chromeOptions = new ChromeOptions();
                        chromeOptions.setHeadless(true);
                        executor = new ChromeDriver(chromeOptions);
                    }
                }
            }
            return executor;
        }
    }

    @Override
    public String getTitle() {
        return element.select(".video-title").html()
                .replace("[原创]", "")
                .replaceAll("/", "")
                .trim();
    }

    @Override
    public String getUrl() {
        String href = element.select(".videos-text-align > a").attr("href");
        if ("".equals(href)) {
            href = element.select(".well > a").attr("href");
        }
        return href;
    }

    @Override
    public String getRealUrl() throws IOException {
        DocumentWrapper documentWrapper = HttpHelper.doHttp(getUrl());
        Document        doc             = documentWrapper.getDocument();
        String          videoEleStr     = doc.select("video > script").html();

        String encoderStr = regexMatch(ENCODE_STR_REGEX, videoEleStr);

        String decoderStr;
        if (encoderStr.contains(",")) {
            String jsStr = script + "return strencode(" + encoderStr + ")";
            decoderStr = (String) ExecutorFactory.getInstance().executeScript(jsStr);
        } else {
            decoderStr = URLDecoder.decode(encoderStr, "UTF-8");
        }
        log.debug("Source: {}", decoderStr);
        String m3u8Src = regexMatch(URL_REGEX, decoderStr);

        log.debug("M3u8 src: {}", m3u8Src);
        return m3u8Src;
    }

    @Override
    protected double duration(String m3u8Url) throws IOException, InterruptedException {
        try {
            String text   = element.select(".duration").text();
            int    length = text.split(":").length;
            if (length == 2) {
                text = "00:" + text;
            } else if (length == 1) {
                text = "00:00" + text;
            }
            LocalTime parse = LocalTime.parse(text);
            return Math.min(parse.getMinute(), 1);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return super.duration(m3u8Url);
        }
    }

    private static String regexMatch(Pattern pattern, String e) {
        Matcher matcher = pattern.matcher(e);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new RuntimeException(e);
    }
}
