package cn.haohaoli.event;

import cn.haohaoli.wapper.ElementWrapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author lwh
 */
@Getter
@RequiredArgsConstructor
public class PagePutEvent {

    private final ElementWrapper wrapper;
}
