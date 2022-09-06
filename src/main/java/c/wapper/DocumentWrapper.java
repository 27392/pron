package c.wapper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jsoup.nodes.Document;

/**
 * @author lwh
 */
@Getter
@AllArgsConstructor(staticName = "of")
public class DocumentWrapper {

    private final Document document;
    private final Type type;

    public enum Type {
        REMOTE,
        CACHE,
    }

}
