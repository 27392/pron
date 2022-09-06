package c.wapper;

import c.utils.HttpHelper;
import c.utils.RegexUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.URLDecoder;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * @author lwh
 */
@Slf4j
@Getter
@AllArgsConstructor
public class DefaultElementWrapper extends AbstractElementWrapper {

    private final Element element;
    private final String  sourceUrl;

    @Override
    public String getTitle() {
        String trim = element.select(".video-title").html()
                .replace("[原创]", "")
                .replaceAll("/", "")
                .replaceAll(" - ", "")
                .trim();
        String s = RegexUtils.id(getUrl());
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

        String encoderStr = RegexUtils.encodeVideoUrl(videoEleStr);
        String decoderStr = URLDecoder.decode(encoderStr, "UTF-8");

        log.debug("Source: {}", decoderStr);
        String m3u8Src = RegexUtils.videoUrl(decoderStr);

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

}
