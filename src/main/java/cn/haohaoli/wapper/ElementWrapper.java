package cn.haohaoli.wapper;

import cn.haohaoli.cache.VideoCache;
import org.jsoup.nodes.Element;

import java.io.Closeable;
import java.nio.file.Path;
import java.time.LocalDate;

/**
 * @author lwh
 */
public interface ElementWrapper extends Closeable {

    /**
     * 获取id
     *
     * @return
     */
    String getId();

    /**
     * 获取元素
     *
     * @return
     */
    Element getElement();

    /**
     * 获取标题
     *
     * @return
     */
    String getTitle();

    /**
     * 页面地址
     *
     * @return
     */
    String getUrl();

    /**
     * 来源地址
     *
     * @return
     */
    String getSourceUrl();

    /**
     * 真实地址
     *
     * @return
     * @throws Exception
     */
    String getRealUrl() throws Exception;

    /**
     * 获取视频时长
     *
     * @return
     * @throws Exception
     */
    double getDuration() throws Exception;

    /**
     * 发布时间
     *
     * @return
     */
    LocalDate getReleaseDate();

    /**
     * 下载超时时间
     *
     * @return
     */
    long timeout();

    /**
     * 是否存在
     *
     * @return
     */
    boolean exist();

    default String getFieldName() {
        return getTitle() + " - " + getId();
    }

    default Path downDir() {
        LocalDate releaseDate = getReleaseDate();
        return VideoCache.CACHE_DIR.resolve(releaseDate.toString());
    }
}
