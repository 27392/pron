package cn.haohaoli.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author lwh
 */
@Slf4j
@UtilityClass
public class RegexUtils {

    private static final Pattern ENCODE_STR_REGEX = Pattern.compile("strencode.+\\((.+?)\\)");
    private static final Pattern URL_REGEX        = Pattern.compile("src='((.+?)(\\.m3u8))'");
    private static final Pattern ID_REGEX         = Pattern.compile("viewkey=(\\w+)");
    private static final Pattern DATE_REGEX         = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})");

    public String id(String url) {
        return regexMatch(ID_REGEX, url);
    }

    public String encodeVideoUrl(String url) {
        return regexMatch(ENCODE_STR_REGEX, url);
    }

    public String videoUrl(String url) {
        return regexMatch(URL_REGEX, url);
    }

    public String videoDate(String path) {
        return regexMatch(DATE_REGEX, path);
    }

    public static String regexMatch(Pattern pattern, String e) {
        Matcher matcher = pattern.matcher(e);
        if (matcher.find()) {
            return matcher.group(1);
        }
        log.error("e: {}", e);
        throw new RuntimeException(e);
    }
}
