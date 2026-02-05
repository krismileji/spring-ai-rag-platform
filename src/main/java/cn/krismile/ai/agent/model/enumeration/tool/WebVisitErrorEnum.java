package cn.krismile.ai.agent.model.enumeration.tool;

import host.springboot.framework3.core.enumeration.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 网页访问错误枚举
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum WebVisitErrorEnum implements BaseEnum<String> {

    /**
     * URL 不合法
     */
    INVALID_URL("INVALID_URL", "URL 不合法"),

    /**
     * 访问频率过高
     */
    RATE_LIMIT("RATE_LIMIT", "访问频率过高"),

    /**
     * HTTP 错误
     */
    HTTP_ERROR("HTTP_ERROR", "HTTP 错误"),

    /**
     * 访问超时
     */
    TIMEOUT("TIMEOUT", "访问超时"),

    /**
     * 解析失败
     */
    PARSE_ERROR("PARSE_ERROR", "解析失败"),

    /**
     * 访问失败
     */
    FETCH_ERROR("FETCH_ERROR", "访问失败");

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举描述
     */
    private final String reasonPhrase;
}
