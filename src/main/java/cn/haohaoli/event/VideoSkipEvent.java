package cn.haohaoli.event;

import cn.haohaoli.wapper.ElementWrapper;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author lwh
 */
@Getter
@AllArgsConstructor
public class VideoSkipEvent {

    private final ElementWrapper wrapper;
}
