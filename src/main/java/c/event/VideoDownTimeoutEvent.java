package c.event;

import c.wapper.ElementWrapper;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author lwh
 */
@Getter
@AllArgsConstructor
public class VideoDownTimeoutEvent {

    private final ElementWrapper wrapper;
}
