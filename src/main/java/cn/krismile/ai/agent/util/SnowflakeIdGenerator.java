package cn.krismile.ai.agent.util;

import java.net.NetworkInterface;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Enumeration;

/**
 * Snowflake ID Generator
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
public class SnowflakeIdGenerator {
    private static final long UNUSED_BITS = 1L;
    private static final long EPOCH_BITS = 41L;
    private static final long NODE_ID_BITS = 10L;
    private static final long SEQUENCE_BITS = 12L;

    private static final long maxNodeId = (1L << NODE_ID_BITS) - 1;
    private static final long maxSequence = (1L << SEQUENCE_BITS) - 1;

    private static final long DEFAULT_CUSTOM_EPOCH = 1420070400000L; // 2015-01-01

    private final long nodeId;
    private final long customEpoch;

    private volatile long lastTimestamp = -1L;
    private volatile long sequence = 0L;

    /**
     * 构造函数
     *
     * @param nodeId 节点ID
     * @param customEpoch 自定义纪元
     * @since 1.0.0
     */
    public SnowflakeIdGenerator(long nodeId, long customEpoch) {
        if (nodeId < 0 || nodeId > maxNodeId) {
            throw new IllegalArgumentException(String.format("NodeId must be between %d and %d", 0, maxNodeId));
        }
        this.nodeId = nodeId;
        this.customEpoch = customEpoch;
    }

    /**
     * 构造函数
     *
     * @param nodeId 节点ID
     * @since 1.0.0
     */
    public SnowflakeIdGenerator(long nodeId) {
        this(nodeId, DEFAULT_CUSTOM_EPOCH);
    }

    /**
     * 构造函数
     *
     * @since 1.0.0
     */
    public SnowflakeIdGenerator() {
        this.nodeId = createNodeId();
        this.customEpoch = DEFAULT_CUSTOM_EPOCH;
    }

    /**
     * 获取下一个ID
     *
     * @return 下一个ID
     * @since 1.0.0
     */
    public synchronized long nextId() {
        long currentTimestamp = timestamp();

        if (currentTimestamp < lastTimestamp) {
            throw new IllegalStateException("Invalid System Clock!");
        }

        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & maxSequence;
            if (sequence == 0) {
                // Sequence Exhausted, wait till next millisecond.
                currentTimestamp = waitNextMillis(currentTimestamp);
            }
        } else {
            // Reset sequence to start with random value to avoid pattern
            sequence = 0L; 
        }

        lastTimestamp = currentTimestamp;

        long id = currentTimestamp << (NODE_ID_BITS + SEQUENCE_BITS)
                | (nodeId << SEQUENCE_BITS)
                | sequence;

        return id;
    }

    /**
     * 获取当前时间戳
     *
     * @return 当前时间戳
     * @since 1.0.0
     */
    private long timestamp() {
        return Instant.now().toEpochMilli() - customEpoch;
    }

    /**
     * 等待下一毫秒
     *
     * @param currentTimestamp 当前时间戳
     * @return 下一毫秒时间戳
     * @since 1.0.0
     */
    private long waitNextMillis(long currentTimestamp) {
        while (currentTimestamp == lastTimestamp) {
            currentTimestamp = timestamp();
        }
        return currentTimestamp;
    }

    /**
     * 创建节点ID
     *
     * @return 节点ID
     * @since 1.0.0
     */
    private long createNodeId() {
        long nodeId;
        try {
            StringBuilder sb = new StringBuilder();
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                byte[] mac = networkInterface.getHardwareAddress();
                if (mac != null) {
                    for (byte macByte : mac) {
                        sb.append(String.format("%02X", macByte));
                    }
                }
            }
            nodeId = sb.toString().hashCode();
        } catch (Exception ex) {
            nodeId = (new SecureRandom().nextInt());
        }
        nodeId = nodeId & maxNodeId;
        return nodeId;
    }
}
