package cn.krismile.ai.agent.util;

import host.springboot.framework3.core.enumeration.error.ErrorCodeEnum;
import host.springboot.framework3.core.exception.ApplicationException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

/**
 * AES加密工具类
 * 用于敏感信息（如API Key）的加密和解密
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
public class AESEncryptionUtil {

    private static final String ALGORITHM = "AES";
    private static final int AES_KEY_SIZE = 32; // 256位密钥

    /**
     * 标准化密钥长度为32字节(AES-256)
     * 使用SHA-256哈希算法将任意长度的密钥转换为32字节
     *
     * @param key 原始密钥
     * @return 32字节的标准密钥
     * @since 1.0.0
     */
    private static byte[] normalizeKey(String key) {
        try {
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            keyBytes = sha.digest(keyBytes);
            return Arrays.copyOf(keyBytes, AES_KEY_SIZE);
        } catch (Exception e) {
            throw new ApplicationException(ErrorCodeEnum.PASSWORD_VERIFY_FAILED, "Failed to normalize key");
        }
    }

    /**
     * 加密字符串
     *
     * @param plainText 明文
     * @param key 密钥
     * @return Base64编码的密文
     * @since 1.0.0
     */
    public static String encrypt(String plainText, String key) {
        try {
            byte[] normalizedKey = normalizeKey(key);
            SecretKeySpec keySpec = new SecretKeySpec(normalizedKey, ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new ApplicationException(ErrorCodeEnum.PASSWORD_VERIFY_FAILED, "Failed to encrypt data");
        }
    }

    /**
     * 解密字符串
     *
     * @param encryptedText Base64编码的密文
     * @param key 密钥
     * @return 明文
     * @since 1.0.0
     */
    public static String decrypt(String encryptedText, String key) {
        try {
            byte[] normalizedKey = normalizeKey(key);
            SecretKeySpec keySpec = new SecretKeySpec(normalizedKey, ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decoded = Base64.getDecoder().decode(encryptedText);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new ApplicationException(ErrorCodeEnum.PASSWORD_VERIFY_FAILED, "Failed to decrypt data");
        }
    }
}
