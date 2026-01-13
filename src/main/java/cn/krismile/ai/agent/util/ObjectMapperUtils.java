package cn.krismile.ai.agent.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import host.springboot.framework3.core.logging.LoggingExecutor;
import host.springboot.framework3.core.model.Pair;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Jackson序列化/反序列化工具类
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
public class ObjectMapperUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectMapperUtils.class);
    private static final String LOG_TAG = "序列化/反序列化工具栏";

    @Setter
    @Getter
    private static ObjectMapper objectMapper = null;

    /**
     * 转换对象
     *
     * @param object        原始对象
     * @param typeReference 目标对象类型
     * @param <T>           目标对象类型
     * @return 目标对象
     * @since 1.0.0
     */
    public static <T> T convertValue(@Nullable Object object, @NonNull TypeReference<T> typeReference) {
        return getObjectMapper().convertValue(object, typeReference);
    }

    /**
     * 反序列化为对象
     *
     * @param object 原始对象
     * @param cls    目标对象类型
     * @param <T>    目标对象类型
     * @return 目标对象
     * @since 1.0.0
     */
    public static <T> T convertValue(@Nullable Object object, @NonNull Class<T> cls) {
        return getObjectMapper().convertValue(object, cls);
    }

    /**
     * 反序列化为对象
     *
     * @param json          JSON 字符串
     * @param typeReference 目标对象类型
     * @param <T>           目标对象类型
     * @return 目标对象
     * @since 1.0.0
     */
    public static <T> T readValueFailSafe(@Nullable String json, @NonNull TypeReference<T> typeReference) {
        try {
            return getObjectMapper().readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            LoggingExecutor.instance().warn(LOGGER, LOG_TAG, "反序列化失败", e, List.of(
                    Pair.of("json", json),
                    Pair.of("typeReference", typeReference)
            ));
            return null;
        }
    }

    /**
     * 反序列化为对象
     *
     * @param json          JSON 字符串
     * @param typeReference 目标对象类型
     * @param <T>           目标对象类型
     * @return 目标对象
     * @throws JsonProcessingException JSON 处理异常
     * @since 1.0.0
     */
    public static <T> T readValue(
            @Nullable String json,
            @NonNull TypeReference<T> typeReference)
            throws JsonProcessingException {
        return getObjectMapper().readValue(json, typeReference);
    }

    /**
     * 反序列化为对象
     *
     * @param json JSON 字符串
     * @param cls  目标对象类型
     * @param <T>  目标对象类型
     * @return 目标对象
     * @since 1.0.0
     */
    public static <T> T readValueFailSafe(@Nullable String json, @NonNull Class<T> cls) {
        try {
            return getObjectMapper().readValue(json, cls);
        } catch (JsonProcessingException e) {
            LoggingExecutor.instance().warn(LOGGER, LOG_TAG, "反序列化失败", e, List.of(
                    Pair.of("json", json),
                    Pair.of("cls", cls)
            ));
            return null;
        }
    }

    /**
     * 反序列化为对象
     *
     * @param json JSON 字符串
     * @param cls  目标对象类型
     * @param <T>  目标对象类型
     * @return 目标对象
     * @throws JsonProcessingException JSON 处理异常
     * @since 1.0.0
     */
    public static <T> T readValue(@Nullable String json, @NonNull Class<T> cls) throws JsonProcessingException {
        return getObjectMapper().readValue(json, cls);
    }

    /**
     * 对象序列化为 JSON 字符串
     *
     * @param object 待序列化的对象
     * @return JSON 字符串
     * @since 1.0.0
     */
    public static String writeValueAsStringFailSafe(@Nullable Object object) {
        try {
            return getObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            LoggingExecutor.instance().warn(LOGGER, LOG_TAG, "序列化失败", e, Pair.of("object", object));
            return null;
        }
    }

    /**
     * 对象序列化为 JSON 字符串
     *
     * @param object 待序列化的对象
     * @return JSON 字符串
     * @throws JsonProcessingException JSON 处理异常
     * @since 1.0.0
     */
    public static String writeValueAsString(@Nullable Object object) throws JsonProcessingException {
        return getObjectMapper().writeValueAsString(object);
    }
}
