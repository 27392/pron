package cn.haohaoli.filter;

import cn.haohaoli.wapper.ElementWrapper;

/**
 * @author lwh
 */
public interface Filter {

    boolean apply( ElementWrapper wrapper) throws Exception;

    int order();
}