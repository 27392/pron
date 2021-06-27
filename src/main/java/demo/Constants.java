package demo;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.LocalDate;

/**
 * Created on 06/27 2021.
 *
 * @author Bennie
 */
public class Constants {

    // Must end with a separator.
    public static final String download_dir = "/Users/bennie/Downloads/91/"
            + LocalDate.now().toString()
            + "/";

    // Set-up your proxy here.
    public static final String socks5_proxy = "socks5://localhost:1080";
    public static       Proxy  proxy        = new Proxy(Proxy.Type.SOCKS,
            new InetSocketAddress("localhost", 1080));

    public static final String UA = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.101 Safari/537.36";

}
