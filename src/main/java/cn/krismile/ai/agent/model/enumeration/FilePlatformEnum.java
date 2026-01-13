package cn.krismile.ai.agent.model.enumeration;

import com.mybatisflex.annotation.EnumValue;
import host.springboot.framework3.core.enumeration.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * FilePlatformEnum
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum FilePlatformEnum implements BaseEnum<String> {

    /**
     * 本地
     */
    LOCAL("LOCAL", "本地"),

    /**
     * 阿里云对象存储
     */
    ALIYUN_OSS("ALIYUN_OSS", "阿里云对象存储");

    /**
     * 枚举值
     */
    @EnumValue
    private final String value;

    /**
     * 枚举描述
     */
    private final String reasonPhrase;

}
