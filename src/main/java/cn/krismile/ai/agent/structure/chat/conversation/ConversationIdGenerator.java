package cn.krismile.ai.agent.structure.chat.conversation;

import host.springboot.framework3.core.logging.LoggingComponent;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 会话ID生成器（已优化：防篡改、长度控制、性能保障）
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Component
public class ConversationIdGenerator implements LoggingComponent {

    private final Mac mac;
    private final SecureRandom random = new SecureRandom();

    public ConversationIdGenerator(@Value("${project.conversation.sign-key}") String signKey) {
        SecretKeySpec keySpec = new SecretKeySpec(signKey.getBytes(), "HmacSHA256");
        try {
            mac = Mac.getInstance("HmacSHA256");
            mac.init(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("HMAC初始化失败", e);
        }
    }

    /**
     * 生成会话 ID
     *
     * @return 会话 ID
     */
    public String generate() {
        long timestamp = System.currentTimeMillis();
        byte[] randomBytes = new byte[8];
        random.nextBytes(randomBytes);
        String randomBase64 = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        String body = timestamp + ":" + randomBase64;
        byte[] signature = mac.doFinal(body.getBytes());

        byte[] truncatedSig = new byte[16];
        System.arraycopy(signature, 0, truncatedSig, 0, 16);
        String sigBase64 = Base64.getUrlEncoder().withoutPadding().encodeToString(truncatedSig);
        return body + ":" + sigBase64;
    }

    /**
     * 验证会话 ID 的合法性
     *
     * @param signedId 会话 ID
     * @return 是否合法
     */
    public boolean verify(String signedId) {
        String[] parts = signedId.split(":");
        if (parts.length != 3) return false;

        try {
            String body = parts[0] + ":" + parts[1];
            byte[] signature = mac.doFinal(body.getBytes());
            byte[] truncatedSig = new byte[16];
            System.arraycopy(signature, 0, truncatedSig, 0, 16);
            String expectedSig = Base64.getUrlEncoder().withoutPadding().encodeToString(truncatedSig);
            return expectedSig.equals(parts[2]);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public @NonNull String logTag() {
        return "会话ID生成器";
    }
}