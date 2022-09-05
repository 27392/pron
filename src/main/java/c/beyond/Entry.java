package c.beyond;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class Entry {

    private final String    title;
    private final LocalDate releaseDate;
    private final double    time;
    private final String    url;
    private final String    realUrl;

    public static Entry of(String[] ary) {
        if (ary.length != 5) {
            return null;
        }
        return new Entry(ary[2], LocalDate.parse(ary[0]), Double.parseDouble(ary[1]), ary[3], ary[4]);
    }

    @Override
    public String toString() {
        return releaseDate + Beyond.SEPARATOR +
                time + Beyond.SEPARATOR +
                title + Beyond.SEPARATOR +
                url + Beyond.SEPARATOR +
                realUrl;
    }

}