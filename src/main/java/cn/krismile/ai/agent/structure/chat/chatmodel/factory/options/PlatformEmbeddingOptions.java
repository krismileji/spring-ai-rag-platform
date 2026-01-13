package cn.krismile.ai.agent.structure.chat.chatmodel.factory.options;

import cn.krismile.ai.agent.structure.chat.model.ChatPlatformDTO;
import cn.krismile.ai.agent.util.PropertyDescriptorUtils;
import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingOptions;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.embedding.EmbeddingOptions;

import java.util.Optional;

/**
 * PlaformEmbeddingOptions
 *
 * @author Lingma
 * @since 1.0.0
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PlatformEmbeddingOptions implements EmbeddingOptions {

    /**
     * 用于嵌入的模型名称
     */
    private @Nullable String model;

    /**
     * 嵌入向量的维度
     */
    private @Nullable Integer dimensions;

    /**
     * 平台信息
     */
    private @NonNull ChatPlatformDTO platform;

    /**
     * DashScope 配置
     */
    private @Nullable DashScopeEmbeddingOptions dashScopeOptions;

    /**
     * 构建器
     *
     * @return Builder
     * @since 1.0.0
     */
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        
        private final PlatformEmbeddingOptions options = new PlatformEmbeddingOptions();

        /**
         * 设置平台信息
         *
         * @param platform 平台信息
         * @return Builder
         * @since 1.0.0
         */
        public @NonNull Builder platform(@NonNull ChatPlatformDTO platform) {
            this.options.setPlatform(platform);
            return this;
        }

        public @NonNull Builder model(@Nullable String model) {
            this.options.setModel(model);
            return this;
        }

        public @NonNull Builder dimensions(@Nullable Integer dimensions) {
            this.options.setDimensions(dimensions);
            return this;
        }

        public @NonNull PlatformEmbeddingOptions build() {
            DashScopeEmbeddingOptions dashScopeOptions = Optional.ofNullable(this.options.dashScopeOptions)
                    .orElseGet(() -> DashScopeEmbeddingOptions.builder().build());

            PropertyDescriptorUtils.copyPropertiesIgnoreNull(this.options, dashScopeOptions);

            this.options.dashScopeOptions = dashScopeOptions;
            return this.options;
        }
    }
}