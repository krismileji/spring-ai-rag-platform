package cn.krismile.ai.agent.configuration.database;

import cn.krismile.ai.agent.model.interfaces.JsonType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * R2DBC Jackson 转换器配置
 * <p>
 * 用于处理实现 JsonType 接口的对象与 JSON 字符串之间的自动转换
 * </p>
 *
 * @author Auto Generated
 * @since 1.0.0
 */
@Slf4j
public class R2dbcJacksonConverters {

    /**
     * 将实现 JsonType 接口的对象转换为 JSON 字符串
     *
     * @author JiYinchuan
     * @since 1.0.0
     */
    @WritingConverter
    @RequiredArgsConstructor
    public static class JsonTypeToJsonConverter implements Converter<JsonType, String> {
        private final ObjectMapper objectMapper;

        @Override
        public String convert(@NonNull JsonType source) {
            try {
                return objectMapper.writeValueAsString(source);
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize object to JSON: {}", source, e);
                throw new RuntimeException("JSON serialization failed for object: " + source, e);
            }
        }
    }

    /**
     * 将 JSON 字符串转换为实现 JsonType 接口的对象
     */
    @ReadingConverter
    @RequiredArgsConstructor
    public static class JsonToJsonTypeConverterFactory implements ConverterFactory<String, JsonType> {
        private final ObjectMapper objectMapper;

        @Override
        public <T extends JsonType> @NonNull Converter<String, T> getConverter(@NonNull Class<T> targetType) {
            return source -> {
                if (!StringUtils.hasText(source)) {
                    return null;
                }
                try {
                    return objectMapper.readValue(source, targetType);
                } catch (IOException e) {
                    log.error("Failed to deserialize JSON to object type: {}", targetType.getName(), e);
                    throw new RuntimeException("JSON deserialization failed for type: " + targetType.getName(), e);
                }
            };
        }
    }

    /**
     * 将 JsonNode 对象转换为 JSON 字符串
     */
    @WritingConverter
    @RequiredArgsConstructor
    public static class JsonNodeToJsonConverter implements Converter<JsonNode, String> {
        private final ObjectMapper objectMapper;

        @Override
        public String convert(@NonNull JsonNode source) {
            try {
                return objectMapper.writeValueAsString(source);
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize JsonNode to JSON: {}", source, e);
                throw new RuntimeException("JSON serialization failed for JsonNode: " + source, e);
            }
        }
    }

    /**
     * 将 JSON 字符串转换为 JsonNode 对象
     *
     * @author JiYinchuan
     * @since 1.0.0
     */
    @ReadingConverter
    @RequiredArgsConstructor
    public static class JsonToJsonNodeConverterFactory implements ConverterFactory<String, JsonNode> {
        private final ObjectMapper objectMapper;

        @Override
        public <T extends JsonNode> @NonNull Converter<String, T> getConverter(@NonNull Class<T> targetType) {
            return source -> {
                if (!StringUtils.hasText(source)) {
                    return null;
                }
                try {
                    return objectMapper.readValue(source, targetType);
                } catch (IOException e) {
                    log.error("Failed to deserialize JSON to JsonNode type: {}", targetType.getName(), e);
                    throw new RuntimeException("JSON deserialization failed for JsonNode type: " + targetType.getName(), e);
                }
            };
        }
    }
}
