package c.wapper;

import c.core.Downloader;
import c.utils.HttpHelper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.URLDecoder;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author lwh
 */
@Slf4j
@Getter
@AllArgsConstructor
public class DefaultElementWrapper extends AbstractElementWrapper {

    private static final Pattern ENCODE_STR_REGEX = Pattern.compile("strencode.+\\((.+?)\\)");
    private static final Pattern URL_REGEX        = Pattern.compile("src='((.+?)(\\.m3u8))'");
    private static final Pattern ID_REGEX         = Pattern.compile("viewkey=(\\w+)");

    private final Element element;
    private final String  sourceUrl;


    @Override
    public String getTitle() {
        String trim = element.select(".video-title").html()
                .replace("[原创]", "")
                .replaceAll("/", "")
                .replaceAll(" - ", "")
                .trim();
        String s = regexMatch(ID_REGEX, getUrl());
        return trim + " - " + s;
    }

    @Override
    public LocalDate getReleaseDate() {
        try {
            DocumentWrapper documentWrapper = HttpHelper.http(getUrl());
            String          text            = documentWrapper.getDocument().select(".title-yakov").first().text();
            return LocalDate.parse(text);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
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
    public String getRealUrl() throws IOException, InterruptedException {
        DocumentWrapper documentWrapper = HttpHelper.http(getUrl());
        Document        doc             = documentWrapper.getDocument();
        String          videoEleStr     = doc.select("video > script").html();

        String encoderStr = regexMatch(ENCODE_STR_REGEX, videoEleStr);
        String decoderStr = URLDecoder.decode(encoderStr, "UTF-8");

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
            return Math.max(parse.getMinute(), 1);
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
        log.error("e: {}", e);
        throw new RuntimeException(e);
    }
}
