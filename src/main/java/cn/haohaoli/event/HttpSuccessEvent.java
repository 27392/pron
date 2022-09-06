package cn.haohaoli.event;

import cn.haohaoli.wapper.DocumentWrapper;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author lwh
 */
@Getter
@AllArgsConstructor
public class HttpSuccessEvent {

    private final DocumentWrapper wrapper;
}
