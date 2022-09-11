package cn.haohaoli.wapper;

import cn.haohaoli.beyond.Beyond;
import cn.haohaoli.beyond.Entry;
import cn.haohaoli.utils.HttpHelper;
import cn.haohaoli.utils.RegexUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.URLDecoder;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.function.Function;

/**
 * @author lwh
 */
@Slf4j
@Getter
public class DefaultElementWrapper extends AbstractElementWrapper {

    private final Element element;
    private final String  sourceUrl;
    private final String  url;

    private LocalDate releaseDate;
    private String    realUrl;

    private static final Function<Element, String> urlFunction = e -> {
        String href = e.select(".videos-text-align > a").attr("href");
        if (StringUtils.isBlank(href)) {
            href = e.select(".well > a").attr("href").replaceFirst("\\?own=[\\d]&", "?");
        } else {
            href = href.substring(0, href.indexOf("&page"));
        }
        return href;
    };

    public DefaultElementWrapper(Element element, String sourceUrl) {
        this(urlFunction.apply(element), element, sourceUrl);
    }

    public DefaultElementWrapper(String url, Element element, String sourceUrl) {
        super(RegexUtils.id(url));
        this.url = url;
        this.element = element;
        this.sourceUrl = sourceUrl;
    }

    @Override
    public String getTitle() {
        return element.select(".video-title").html()
                .replace("[原创]", "")
                .replaceAll("/", "")
                .replaceAll(" - ", "")
                .trim();
    }

    @Override
    public LocalDate getReleaseDate() {
        if (this.releaseDate != null) {
            return this.releaseDate;
        }
        Entry entry = Beyond.get(url);
        if (entry != null) {
            this.releaseDate = entry.getReleaseDate();
        }
        if (this.releaseDate == null) {
            try {
                DocumentWrapper documentWrapper = HttpHelper.http(getUrl());
                String          text            = documentWrapper.getDocument().select(".title-yakov").first().text();
                this.releaseDate = LocalDate.parse(text);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return this.releaseDate;
    }

    @Override
    public String getRealUrl() throws IOException, InterruptedException {
        if (this.realUrl != null) {
            return this.realUrl;
        }
        Entry entry = Beyond.get(this.url);
        if (entry != null) {
            this.realUrl = entry.getRealUrl();
        }
        if (this.realUrl == null) {
            DocumentWrapper documentWrapper = HttpHelper.http(getUrl());
            Document        doc             = documentWrapper.getDocument();
            String          videoEleStr     = doc.select("video > script").html();

            String encoderStr = RegexUtils.encodeVideoUrl(videoEleStr);
            String decoderStr = URLDecoder.decode(encoderStr, "UTF-8");
            log.debug("Source: {}", decoderStr);

            this.realUrl = RegexUtils.videoUrl(decoderStr);
            log.debug("M3u8 src: {}", this.realUrl);
        }
        return this.realUrl;
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
