package c.beyond;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Entry {

    private final String title;
    private final double time;
    private final String url;
    private final String realUrl;

    public static Entry of(String[] ary) {
        if (ary.length != 4) {
            return null;
        }
        return new Entry(ary[0], Double.parseDouble(ary[1]), ary[2], ary[3]);
    }

    @Override
    public String toString() {
        return title + Beyond.SEPARATOR +
                time + Beyond.SEPARATOR +
                url + Beyond.SEPARATOR +
                realUrl;
    }

}