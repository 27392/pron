package c.utils;

import lombok.experimental.UtilityClass;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
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

    /**
     * 扫描文件
     *
     * @param path
     * @param predicate
     * @param consumer
     */
    public void scanFile(Path path, Predicate<File> predicate, Consumer<File> consumer) {
        File[] files = getFilesBySortCreate(path.toFile());
        if (files == null) {
            return;
        }
        LinkedList<File> queue = new LinkedList<>(Arrays.asList(files));

        while (!queue.isEmpty()) {
            File f = queue.removeLast();
            if (f.isDirectory()) {
                File[] fs = getFilesBySortCreate(f);
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
            return lines.collect(Collectors.joining());
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
            return lines.map(function)
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
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file, append)))) {
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
     * 获取创建时间
     *
     * @param file
     * @return
     */
    public long getCreateTime(File file) {
        try {
            BasicFileAttributes basicFileAttributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            return basicFileAttributes.creationTime().toMillis();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public File[] getFilesBySortCreate (File file) {
        File[] files = file.listFiles();
        if (files != null) {
            Arrays.sort(files, (f1, f2) -> (int) (getCreateTime(f2) - getCreateTime(f1)));
        }
        return files;
    }
}
