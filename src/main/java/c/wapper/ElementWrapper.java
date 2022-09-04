package c.wapper;

import org.jsoup.nodes.Element;


/**
 * @author lwh
 */
public interface ElementWrapper {

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
    Double getDuration() throws Exception;

    /**
     * 下载超时时间
     *
     * @return
     */
    long timeout();
}
