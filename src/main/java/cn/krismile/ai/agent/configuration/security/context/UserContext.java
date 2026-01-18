package cn.krismile.ai.agent.configuration.security.context;

import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * User Context for Synchronous Access
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
public class UserContext {

    private static final TransmittableThreadLocal<Long> USER_ID_HOLDER = new TransmittableThreadLocal<>();

    public static void setUserId(Long userId) {
        USER_ID_HOLDER.set(userId);
    }

    public static Long getUserId() {
        return USER_ID_HOLDER.get();
    }

    public static void clear() {
        USER_ID_HOLDER.remove();
    }
}
