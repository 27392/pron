package cn.haohaoli.proxy;

import cn.haohaoli.wapper.ElementWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

/**
 * @author lwh
 */
@Slf4j
public class ElementWrapperProxy {

    public static ElementWrapper create(ElementWrapper wrapper) {
        return (ElementWrapper) Proxy.newProxyInstance(wrapper.getClass().getClassLoader(),
                new Class[]{ElementWrapper.class},
                new MDCHandler(wrapper));
    }

    @RequiredArgsConstructor
    static class MDCHandler implements InvocationHandler {

        private final ElementWrapper target;

        @Override
        public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
            if (MDC.get("id") == null) {
                MDC.put("id", StringUtils.rightPad(target.getId(), 20));
                MDC.put("releaseDate", Objects.toString(target.getReleaseDate()));
                MDC.put("duration", StringUtils.leftPad((Objects.toString((int) target.getDuration())), 2, "0"));
            }
            return method.invoke(target, objects);
        }
    }
}