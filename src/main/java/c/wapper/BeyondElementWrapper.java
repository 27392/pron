package c.wapper;

import c.beyond.Entry;
import c.utils.Pool;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Element;

/**
 * @author lwh
 */
@RequiredArgsConstructor
public class BeyondElementWrapper extends AbstractElementWrapper {

    private final Entry entry;

    @Override
    public Element getElement() {
        return null;
    }

    @Override
    public String getTitle() {
        return entry.getTitle();
    }

    @Override
    public String getUrl() {
        return entry.getUrl();
    }

    @Override
    public String getSourceUrl() {
        return "Beyond&page=Beyond";
    }

    @Override
    public String getRealUrl() throws Exception {
        return entry.getRealUrl();
    }

    @Override
    public long timeout() {
        try {
            long i = (long) (getDuration().intValue() * 0.25);
            if (i <= super.timeout()) {
                return super.timeout();
            }
            return Math.min(i, (Pool.getTimeOut()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.timeout();
    }

    @Override
    public Double getDuration() {
        return entry.getTime();
    }
}
