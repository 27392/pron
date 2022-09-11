package cn.haohaoli.cmmon;

/**
 * @author lwh
 */
public class Const {

    public static final String BEYOND_SEPARATOR = " ## ";
    public static final String PAGE = "&page=";

    public static final String HOST = "https://91porn.com/";

    public static final String HTML_SUFFIX   = ".html";
    public static final String HTML_DIR_NAME = "html";

    public static final String VIDEO_DIR_NAME = "video";
    public static final String VIDEO_SUFFIX   = ".mp4";

    public static final String UA = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.101 Safari/537.36";

    @Deprecated
    public static final String PROXY = "export https_proxy=http://{0} http_proxy=http://{0} all_proxy=socks5://{0}";

    public static final String OUTPUT_MP4_COMMAND = "ffmpeg -y -i \"%s\" -acodec copy -vcodec copy \"%s\"";
    public static final String DURATION_COMMAND   = "ffprobe -i \"%s\" -show_entries format=duration -v quiet -of csv='p=0'";
}
