package c.utils;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author lwh
 */
@UtilityClass
public class FileUtils {

    Base64.Encoder encoder = Base64.getEncoder();
    Base64.Decoder decoder = Base64.getDecoder();

    /**
     * 扫描文件
     *
     * @param path
     * @param predicate
     * @param consumer
     */
    public void scanFile(Path path, Predicate<File> predicate, Consumer<File> consumer) {
        File[] files = path.toFile().listFiles();
        if (files == null) {
            return;
        }
        LinkedList<File> queue = new LinkedList<>(Arrays.asList(files));

        while (!queue.isEmpty()) {
            File f = queue.removeLast();
            if (f.isDirectory()) {
                File[] fs = f.listFiles();
                if (fs != null) {
                    for (File item : fs) {
                        queue.push(item);
                    }
                }
            } else {
                if (predicate.test(f)) {
                    consumer.accept(f);
                }
            }
        }
    }

    /**
     * 读取文件
     *
     * @param file
     * @return
     */
    public String readToString(File file) {
        if (file == null) {
            return null;
        }
        try (Stream<String> lines = Files.lines(file.toPath())) {
            return decode(lines.collect(Collectors.joining()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 读取文件
     *
     * @param file
     * @param function
     * @param collector
     * @param <T>
     * @param <A>
     * @param <R>
     * @return
     * @throws IOException
     */
    public <T, A, R> R read(File file, Function<String, T> function, Collector<? super T, A, R> collector) throws IOException {
        if (file == null) {
            return null;
        }
        try (Stream<String> lines = Files.lines(file.toPath())) {
            return lines.map(FileUtils::decode)
                    .map(function)
                    .filter(Objects::nonNull)
                    .collect(collector);
        }
    }

    /**
     * 写出文件
     *
     * @param file
     * @param append
     * @param consumer
     */
    public void writer(File file, boolean append, Consumer<PrintWriter> consumer) throws IOException {
        try (EncoderPrintWriter writer = new EncoderPrintWriter(new BufferedWriter(new FileWriter(file, append)))) {
            consumer.accept(writer);
        }
    }

    /**
     * 写出文件
     *
     * @param file
     * @param append
     * @param context
     * @throws IOException
     */
    public void writer(File file, boolean append, String context) throws IOException {
        writer(file, append, (w) -> w.println(context));
    }


    /**
     * 解码
     *
     * @param str
     * @return
     */
    private String decode(String str) {
        if (str == null) {
            return null;
        }
        try {
            return new String(decoder.decode(str.getBytes(StandardCharsets.UTF_8)));
        } catch (IllegalArgumentException e) {
            return str;
        }
    }
    
    private class EncoderPrintWriter extends PrintWriter {

        public EncoderPrintWriter(@NotNull Writer out) {
            super(out);
        }

        @Override
        public void println(String x) {
            String str = encoder.encodeToString(x.getBytes(StandardCharsets.UTF_8));
            super.println(str);
        }

        @Override
        public void println(Object x) {
            String str = encoder.encodeToString(x.toString().getBytes(StandardCharsets.UTF_8));
            super.println(str);
        }
    }

}
