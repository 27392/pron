package cn.haohaoli.component;

import com.google.common.eventbus.EventBus;

/**
 * @author lwh
 */
@SuppressWarnings("all")
public class EventPublisher {

    static class EventBusHolder {
        private static EventBus INSTANCE = new EventBus();
    }

    public static void publish(Object o){
        EventBusHolder.INSTANCE.post(o);
    }

    public static void register(Object o){
        EventBusHolder.INSTANCE.register(o);
    }
}
