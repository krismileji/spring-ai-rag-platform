package cn.krismile.ai.agent.exception;

/**
 * 会话安全异常
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
public class ConversationSecurityException extends RuntimeException {

    public ConversationSecurityException(String message) {
        super(message);
    }

    public ConversationSecurityException(String message, Throwable cause) {
        super(message, cause);
    }
}
