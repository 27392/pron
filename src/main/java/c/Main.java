package c;

import c.core.TypeEnum;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * @author lwh
 */
@Slf4j
public class Main {

    static final TypeEnum typeEnum = TypeEnum.BOUTIQUE;

    static {
        // 下载地址
        System.setProperty("downloadDir", "/Users/liwenhao/Downloads/91");
        // 最大页数
        System.setProperty("maxPage", "20");
        // 最大时长
        System.setProperty("maxDuration", "25");
        // 下载超时
        System.setProperty("downloadTimeout", "6");

        System.setProperty("webdriver.chrome.driver", "/Users/liwenhao/Downloads/chromedriver");
    }

    public static void main(String[] args) throws InterruptedException, URISyntaxException, IOException {
        typeEnum.start(new ArrayBlockingQueue<>(((4 * 6) * 3)), 5);
    }

}
