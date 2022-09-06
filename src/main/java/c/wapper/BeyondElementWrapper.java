package c.wapper;

import c.beyond.Entry;
import c.utils.TaskUtils;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Element;

import java.time.LocalDate;

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
    public String getRealUrl() {
        return entry.getRealUrl();
    }

    @Override
    public long timeout() {
        try {
            long i = (long) (this.getDuration() * 0.25);
            if (i <= super.timeout()) {
                return super.timeout();
            }
            return Math.min(i, (TaskUtils.getTimeOut()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.timeout();
    }

    @Override
    public double getDuration() {
        return entry.getTime();
    }

    @Override
    public LocalDate getReleaseDate() {
        return entry.getReleaseDate();
    }
}
