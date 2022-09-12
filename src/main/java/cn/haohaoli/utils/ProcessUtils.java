package cn.haohaoli.utils;

import cn.haohaoli.cmmon.Const;
import cn.haohaoli.config.Config;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static cn.haohaoli.cmmon.Const.*;

/**
 * @author lwh
 */
@Slf4j
@UtilityClass
public class ProcessUtils {

    private static final String OS_NAME = System.getProperties().getProperty(OS);

    /**
     * 执行命令
     *
     * @param timeout
     * @param command
     * @return
     * @throws InterruptedException
     * @throws IOException
     */
    public Result doExecute(int timeout, String command) throws InterruptedException, IOException {
        ProcessBuilder builder;

        if (OS_NAME.contains(WINDOWS_OS)) {
            builder = new ProcessBuilder("cmd.exe", "/c", command);
        } else {
            builder = new ProcessBuilder("/bin/sh", "-c", command);
        }
        builder.redirectErrorStream(true);

        log.debug("command: {}", builder.command());
        Process p = builder.start();
        if (timeout <= 0) {
            return Result.of(p.waitFor(), getResult(p));
        } else {
            if (p.waitFor(timeout, TimeUnit.MINUTES)) {
                return Result.of(p.waitFor(), getResult(p));
            } else {
                try {
                    return Result.of(-1, getResult(p));
                } finally {
                    p.destroyForcibly();
                }
            }

        }
    }

    /**
     * 执行命令
     *
     * @param timeout
     * @param command
     * @return
     * @throws InterruptedException
     * @throws IOException
     */
    public Result execute(int timeout, String command) throws InterruptedException, IOException {
        return doExecute(timeout, command);
    }

    /**
     * 执行命令并获取返回值
     *
     * @param command
     * @return
     * @throws InterruptedException
     * @throws IOException
     */
    public String executeResult(String command) throws InterruptedException, IOException {
        return doExecute(0, command).msg;
    }

    /**
     * 下载mp4
     *
     * @param timeout
     * @param dir
     * @param name
     * @param m3u8Url
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public Result outputToMp4(int timeout, Path dir, String name, String m3u8Url) throws IOException, InterruptedException {
        Path   directories = Files.createDirectories(dir).resolve(name + Const.VIDEO_SUFFIX);
        String cmd         = String.format(OUTPUT_MP4_COMMAND, m3u8Url, directories.toString());
        return execute(timeout, Config.getFfmpegDir() + "/" + cmd);
    }

    /**
     * 获取视频时长
     *
     * @param m3u8Url
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public double duration(String m3u8Url) throws IOException, InterruptedException {
        String cmd    = String.format(DURATION_COMMAND, m3u8Url);
        String result = executeResult(Config.getFfmpegDir() + "/" + cmd);
        return new BigDecimal(result).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_EVEN).doubleValue();
    }

    /**
     * 获取结果
     *
     * @param process
     * @return
     * @throws IOException
     */
    public String getResult(Process process) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String str;
            while ((str = bufferedReader.readLine()) != null) {
                sb.append(str);
            }
        }
        return sb.toString();
    }

    @Getter
    @AllArgsConstructor(staticName = "of")
    public static class Result {

        private final int    code;
        private final String msg;
    }

}
