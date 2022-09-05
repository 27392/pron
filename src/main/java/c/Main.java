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

    public static void main(String[] args) throws InterruptedException, URISyntaxException, IOException {
        TypeEnum type = Config.getType();
        log.info("{}", type);

        type.start(new ArrayBlockingQueue<>(((4 * 6) * 3)), Config.getDownCount());
    }

}
