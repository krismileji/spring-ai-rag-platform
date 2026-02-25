package cn.krismile.ai.agent.model.enumeration.chat;

import com.fasterxml.jackson.annotation.JsonCreator;
import host.springboot.framework3.core.enumeration.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * 聊天模型模态类型枚举
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum ChatModelModalityEnum implements BaseEnum<String> {

    /**
     * 文本
     */
    TEXT("TEXT", "", List.of("Text")),

    /**
     * 图片
     */
    IMAGE("IMAGE", "", List.of("Image")),

    /**
     * 视频
     */
    VIDEO("VIDEO", "", List.of("Video"));

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举描述
     */
    private final String reasonPhrase;

    /**
     * 类型
     */
    private final List<String> types;

    /**
     * 根据类型获取枚举
     *
     * @param type 类型
     * @return 枚举
     * @since 1.0.0
     */
    public static @Nullable ChatModelModalityEnum ofType(String type) {
        return Arrays.stream(ChatModelModalityEnum.values())
                .filter(e -> e.types.contains(type))
                .findFirst()
                .orElse(null);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static @Nullable ChatModelModalityEnum of(String value) {
        return BaseEnum.parse(value, ChatModelModalityEnum.class);
    }
}