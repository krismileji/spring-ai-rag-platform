package cn.krismile.ai.agent.exception.tool;

import cn.krismile.ai.agent.model.enumeration.tool.WebVisitErrorEnum;
import host.springboot.framework3.core.exception.ApplicationException;
import lombok.Getter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * 网页访问异常
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Getter
public class WebVisitException extends ApplicationException {

    private final Integer status;
    private final boolean retryable;

    /**
     * 构造异常
     *
     * @param errorEnum 错误枚举
     * @param retryable 是否可重试
     * @since 1.0.0
     */
    public WebVisitException(@NonNull WebVisitErrorEnum errorEnum, boolean retryable) {
        this(errorEnum, errorEnum.getReasonPhrase(), null, retryable);
    }

    /**
     * 构造异常
     *
     * @param errorEnum    错误枚举
     * @param errorMessage 错误信息
     * @param httpStatus   Http 状态码
     * @param retryable    是否可重试
     * @since 1.0.0
     */
    public WebVisitException(@NonNull WebVisitErrorEnum errorEnum, @NonNull String errorMessage, @Nullable Integer httpStatus, boolean retryable) {
        super(errorEnum, errorMessage);
        this.status = httpStatus;
        this.retryable = retryable;
    }
}
