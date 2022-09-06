package c.utils;

import c.cache.VideoCache;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author lwh
 */
@Slf4j
@UtilityClass
public class ProcessUtils {

    private static final String OUTPUT_MP4 = "export https_proxy=http://127.0.0.1:7890 http_proxy=http://127.0.0.1:7890 all_proxy=socks5://127.0.0.1:7890 && ffmpeg -y -i '%s' -acodec copy -vcodec copy '%s'";
    private static final String DURATION   = "export https_proxy=http://127.0.0.1:7890 http_proxy=http://127.0.0.1:7890 all_proxy=socks5://127.0.0.1:7890 && ffprobe -i '%s' -show_entries format=duration -v quiet -of csv='p=0'";

    public Process doExecute(String command) throws InterruptedException, IOException {

        ProcessBuilder builder = new ProcessBuilder("/bin/sh", "-c", command);
        Process        p       = builder.start();
        int            i       = p.waitFor();
        if (i != 0) {
            log.warn("cmd: {}", command);
        }
        return p;
    }

    public int execute(String command) throws InterruptedException, IOException {
        return doExecute(command).waitFor();
    }

    public String executeResult(String command) throws InterruptedException, IOException {
        Process process = doExecute(command);

        StringBuilder sb = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String str;
            while ((str = bufferedReader.readLine()) != null) {
                sb.append(str);
            }
        }
        return sb.toString();
    }

    public int outputToMp4(Path dir, String title, String m3u8Url) throws IOException, InterruptedException {
        Path   directories = Files.createDirectories(dir).resolve(title + VideoCache.SUFFIX);
        String cmd         = String.format(OUTPUT_MP4, m3u8Url, directories.toString());
        return execute(cmd);
    }


    public double duration(String m3u8Url) throws IOException, InterruptedException {
        String cmd    = String.format(DURATION, m3u8Url);
        String result = executeResult(cmd);
        return new BigDecimal(result).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_EVEN).doubleValue();

    }


}
