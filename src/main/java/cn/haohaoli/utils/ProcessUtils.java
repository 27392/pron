package cn.haohaoli.utils;

import cn.haohaoli.cmmon.Const;
import cn.haohaoli.config.Config;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiFunction;

import static cn.haohaoli.cmmon.Const.*;

/**
 * @author lwh
 */
@Slf4j
@UtilityClass
public class ProcessUtils {

    private static final String OS_NAME = System.getProperties().getProperty(OS);

    private static final BiFunction<String, String, String> f = (c, s) -> StringUtils.isBlank(Config.getProxyCommand()) ? c : Config.getProxyCommand() + s + c;

    /**
     * 执行命令
     *
     * @param command
     * @return
     * @throws InterruptedException
     * @throws IOException
     */
    public Process doExecute(String command) throws InterruptedException, IOException {
        ProcessBuilder builder;

        if (OS_NAME.contains(WINDOWS_OS)) {
            builder = new ProcessBuilder("cmd.exe", "/c", f.apply(command, WINDOWS_PROXY_JOIN));
        } else {
            builder = new ProcessBuilder("/bin/sh", "-c", f.apply(command, MAC_PROXY_JOIN));
        }
        builder.redirectErrorStream(true);

        log.debug("command: {}", builder.command());
        Process p = builder.start();
        int     i = p.waitFor();
        if (i != 0) {
            log.warn("result: {}", getResult(p));
        }
        return p;
    }

    /**
     * 执行命令
     *
     * @param command
     * @return
     * @throws InterruptedException
     * @throws IOException
     */
    public int execute(String command) throws InterruptedException, IOException {
        return doExecute(command).waitFor();
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
        Process process = doExecute(command);
        return getResult(process);
    }

    /**
     * 下载mp4
     *
     * @param dir
     * @param name
     * @param m3u8Url
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public int outputToMp4(Path dir, String name, String m3u8Url) throws IOException, InterruptedException {
        Path   directories = Files.createDirectories(dir).resolve(name + Const.VIDEO_SUFFIX);
        String cmd         = String.format(OUTPUT_MP4_COMMAND, m3u8Url, directories.toString());
        return execute(Config.getFfmpegDir() + File.separator + cmd);
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
        String result = executeResult(Config.getFfmpegDir() + File.separator + cmd);
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


}
