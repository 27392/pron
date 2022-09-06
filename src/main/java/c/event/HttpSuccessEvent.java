package c.event;

import c.wapper.DocumentWrapper;
import c.wapper.ElementWrapper;
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
