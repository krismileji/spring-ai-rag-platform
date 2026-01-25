package cn.krismile.ai.agent.constant;

import cn.krismile.ai.agent.model.enumeration.chat.ChatModelTypeEnum;
import cn.krismile.ai.agent.model.enumeration.chat.ChatPlatformEnum;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Duration;

/**
 * Redis key constants
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
public interface RedisKey {

    static Key chatModel(ChatPlatformEnum platform, ChatModelTypeEnum modelType) {
        return new Key(null, "ai", "platform", "models", platform.getValue(), modelType.getValue());
    }

    /**
     * 键类
     *
     * @author JiYinchuan
     * @since 1.0.0
     */
    record Key(@Nullable Duration expire, @NonNull String key) {
        public Key(@Nullable Duration expire, @NonNull String... key) {
            this(expire, String.join(":", key));
        }
    }
}
