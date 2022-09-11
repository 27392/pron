package cn.haohaoli.filter;

import cn.haohaoli.wapper.ElementWrapper;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author lwh
 */
@Slf4j
public class FilterExecutor implements Filter {

    private final List<Filter> filters = new ArrayList<>();

    {
        filters.add(new CacheExistFilter());
        filters.add(new DurationLongFilter());
        filters.add(new ExpiredFilter());
        filters.add(new RealUrlFilter());

        filters.sort(Comparator.comparingInt(Filter::order));
    }

    static class InstanceHolder {
        static final FilterExecutor INSTANCE = new FilterExecutor();
    }

    public static FilterExecutor getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public boolean apply(ElementWrapper wrapper) {
        for (Filter filter : filters) {
            try {
                boolean apply = filter.apply(wrapper);
                if (!apply) {
                    return false;
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return true;
    }

    @Override
    public int order() {
        return -1;
    }
}
