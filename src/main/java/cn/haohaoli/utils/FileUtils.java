package cn.haohaoli.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
@UtilityClass
public class FileUtils {

    /**
     * 扫描文件
     *
     * @param path
     * @param predicate
     * @param consumer
     * @param sort
     */
    public void scanFile(Path path, Predicate<File> predicate, Consumer<File> consumer, FileSort sort) {
        File[] files = sort.getFiles(path.toFile());
        if (files == null) {
            return;
        }
        LinkedList<File> queue = new LinkedList<>(Arrays.asList(files));

        while (!queue.isEmpty()) {
            File f = queue.removeLast();
            if (f.isDirectory()) {
                File[] fs = sort.getFiles(f);
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

    public void scanFile(Path path, Predicate<File> predicate, Consumer<File> consumer) {
        scanFile(path, predicate, consumer, FileSort.NONE);
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

    public void writer(File file, boolean append, Collection<?> contexts) throws IOException {
        writer(file, append, (w) -> {
            for (Object context : contexts) {
                w.println(context);
            }
        });
    }

    /**
     * 获取属性
     *
     * @param file
     * @return
     */
    public BasicFileAttributes getAttributes(File file) {
        try {
            return Files.readAttributes(file.toPath(), BasicFileAttributes.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取创建时间
     *
     * @param file
     * @return
     */
    public long getCreateTime(File file) {
        try {
            return getAttributes(file).creationTime().toMillis();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 存在获取,不存在创建
     *
     * @param file
     * @return
     * @throws IOException
     */
    public File getOrCreate(File file) throws IOException {
        if (file.isFile()) {
            return file;
        }
        Path path = Files.createFile(file.toPath());
        return path.toFile();
    }

    /**
     * 如果存在则删除重新创建
     *
     * @param del
     * @param file
     * @return
     * @throws IOException
     */
    public File create(boolean del, File file) throws IOException {
        if (file.isFile() && del) {
            delete(file);
        }
        Path path = Files.createFile(file.toPath());
        return path.toFile();
    }

    public void renameTo(File scr, File dest) {
        delete(dest);
        boolean b = scr.renameTo(dest);
    }

    /**
     * 删除文件
     *
     * @param file
     * @return
     */
    public boolean delete(File file) {
        try {
            Files.delete(file.toPath());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    public enum FileSort {

        CREATE_TIME_ASC {
            @Override
            public File[] getFiles(File file) {
                File[] files = file.listFiles();
                if (files != null) {
                    Arrays.sort(files, (f1, f2) -> (int) (getCreateTime(f1) - getCreateTime(f2)));
                }
                return files;
            }
        },
        CREATE_TIME_DESC {
            @Override
            public File[] getFiles(File file) {
                File[] files = file.listFiles();
                if (files != null) {
                    Arrays.sort(files, (f1, f2) -> (int) (getCreateTime(f2) - getCreateTime(f1)));
                }
                return files;
            }
        },
        NONE {
            @Override
            public File[] getFiles(File file) {
                return file.listFiles();
            }
        },
        NAME {
            @Override
            public File[] getFiles(File file) {
                File[] files = file.listFiles();
                if (files != null) {
                    Arrays.sort(files, Comparator.comparing(File::getName));
                }
                return files;
            }
        };

        public abstract File[] getFiles(File file);
    }
}
