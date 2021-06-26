package demo;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URLDecoder;

import static org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY;

/**
 * Created on 06/20 2021.
 *
 * @author Bennie
 */
public class Main {

    static {
        System.setProperty(DEFAULT_LOG_LEVEL_KEY, "DEBUG");
    }

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static final String Video_Url = "" +
            "https://91porn.com/view_video.php?viewkey=1e5bd08b716832ea8112";

    // Must end with a separator.
    private static final String download_dir = "/Users/bennie/Downloads/91/";

    // set-up your proxy here.
    private static final String socks5_proxy = "socks5://localhost:1080";
    private static       Proxy  proxy        = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("localhost", 1080));

    private static final String UA = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.101 Safari/537.36";

    public static void main(String[] args) throws Exception {

        Document doc = getDoc(Video_Url, UA, proxy);
        if (doc.location().contains("404.html")) {
            logger.info("Video has been deleted.");
            return;
        }

        String title = doc.title();
        logger.info("video: {}", title);

        String videoEleStr = doc.select("video > script").html();
        String beginStr    = "strencode2(\"";
        int    begin       = videoEleStr.indexOf("strencode2(\"");
        int    end         = videoEleStr.indexOf("\"));");

        String substring = videoEleStr.substring(begin + beginStr.length(), end);
        logger.debug("substring: {}", substring);

        String source = URLDecoder.decode(substring, "UTF-8");
        logger.debug("source: {}", source);

        Document parse   = Jsoup.parse(source);
        String   m3u8Src = parse.select("source").attr("src");
        logger.info("m3u8 src: {}", m3u8Src);

        outputToMp4(m3u8Src, title);
    }

    private static Document getDoc(String url, String ua, Proxy proxy) throws Exception {
        return Jsoup.connect(url)
                .userAgent(ua)
                .header("Accept-Language", "zh-CN")
                .proxy(proxy).get();
    }


    private static void outputToMp4(String m3u8Url, String title) throws Exception {
        String outputPath = download_dir + title + ".mp4";
        String _cmd       = "export all_proxy=%s && ffmpeg -i '%s' -acodec copy -vcodec copy '%s'";
        String cmd        = String.format(_cmd, socks5_proxy, m3u8Url, outputPath);
        logger.debug("cmd: {}", cmd);

        ProcessBuilder builder = new ProcessBuilder("/bin/sh", "-c", cmd);
        Process        p       = builder.start();
        logger.info("Working now, please wait for a while...");

        InputStream inputStream = p.getInputStream();
        // ----
        String s;

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(inputStream));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        while ((s = stdInput.readLine()) != null) {
            logger.info(s);
        }
        while ((s = stdError.readLine()) != null) {
            logger.info(s);
        }
        int exitValue;
        try {
            exitValue = p.waitFor();
            logger.info("processor returning code {}", exitValue);
        } catch (InterruptedException e) {
            logger.error("error", e);
        }

        closeStream(stdInput);
        closeStream(stdError);
    }

    private static void closeStream(BufferedReader reader) {
        try {
            if (reader != null) reader.close();
        } catch (Exception e) {
        }
    }
}