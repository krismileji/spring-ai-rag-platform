package cn.krismile.ai.agent.configuration.security.context;

import io.micrometer.context.ThreadLocalAccessor;
import org.jspecify.annotations.NonNull;

/**
 * UserContext ThreadLocalAccessor
 * <p>
 * Bridges Reactor Context and ThreadLocal for User ID propagation.
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
public class UserContextThreadLocalAccessor implements ThreadLocalAccessor<Long> {

    public static final String KEY = "USER_ID";

    @Override
    public @NonNull Object key() {
        return KEY;
    }

    @Override
    public Long getValue() {
        return UserContext.getUserId();
    }

    @Override
    public void setValue(@NonNull Long value) {
        UserContext.setUserId(value);
    }

    @Override
    public void reset() {
        UserContext.clear();
    }
}
