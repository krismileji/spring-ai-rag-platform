package cn.krismile.ai.agent.model.enumeration;

import com.mybatisflex.annotation.EnumValue;
import host.springboot.framework3.core.enumeration.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户状态枚举
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum UserStatusEnum implements BaseEnum<String> {

    /**
     * 正常
     */
    NORMAL("NORMAL", "正常"),

    /**
     * 禁用
     */
    DISABLED("DISABLED", "禁用")

    ;

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
