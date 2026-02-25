package cn.krismile.ai.agent.structure.chat.chatmodel.factory.options;

import cn.krismile.ai.agent.model.response.chat.ChatModelMetaDataVO;
import cn.krismile.ai.agent.structure.chat.model.ChatPlatformDTO;
import cn.krismile.ai.agent.util.PropertyDescriptorUtils;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.ollama.api.ThinkOption;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.util.Assert;

import java.util.*;

/**
 * PlatformChatOptions
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PlatformChatOptions implements ToolCallingChatOptions {

    /**
     * 注册到聊天模型中的工具回调函数列表
     */
    private List<ToolCallback> toolCallbacks = new ArrayList<>();

    /**
     * 注册到聊天模型中的工具名称集合
     */
    private Set<String> toolNames = new HashSet<>();

    /**
     * 工具上下文映射表，提供给工具使用的额外上下文数据
     */
    private Map<String, Object> toolContext = new HashMap<>();

    /**
     * 聊天模型是否负责执行由模型请求的工具，还是由调用者直接执行工具（默认值为 {@value DEFAULT_TOOL_EXECUTION_ENABLED}）
     */
    private @Nullable Boolean internalToolExecutionEnabled;

    /**
     * 用于聊天的模型名称
     */
    private @Nullable String model;

    /**
     * 频率惩罚值，用于减少重复内容的生成
     */
    private @Nullable Double frequencyPenalty;

    /**
     * 聊天中使用的最大令牌数（token）
     */
    private @Nullable Integer maxTokens;

    /**
     * 存在性惩罚值，用于鼓励话题转换
     */
    private @Nullable Double presencePenalty;

    /**
     * 停止序列，指定模型应在何处停止生成文本
     */
    private @Nullable List<String> stopSequences;

    /**
     * 温度参数，控制输出的随机性和创造性
     */
    private @Nullable Double temperature;

    /**
     * Top-K采样参数，限制从最可能的K个词汇中选择
     */
    private @Nullable Integer topK;

    /**
     * Top-P采样参数，选择累积概率达到P的词汇子集
     */
    private @Nullable Double topP;

    /**
     * 控制模型在生成文本时是否引用和使用互联网搜索结果
     */
    private @Nullable Boolean enableSearch;

    /**
     * 是否启用模型的思维过程
     */
    private @Nullable Boolean enableThinking;

    /**
     * 模型元数据
     */
    private @Nullable ChatModelMetaDataVO metaData;

    /**
     * 平台信息
     */
    private @NonNull ChatPlatformDTO platform;

    /**
     * DashScope 配置
     */
    private @Nullable DashScopeChatOptions dashScopeOptions;

    /**
     * DeepSeek 配置
     */
    private @Nullable DeepSeekChatOptions deepSeekOptions;

    /**
     * Ollama 配置
     */
    private @Nullable OllamaChatOptions ollamaOptions;

    /**
     * 构建器
     *
     * @return Builder
     * @since 1.0.0
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    @SuppressWarnings("unchecked")
    public PlatformChatOptions copy() {
        PlatformChatOptions options = PlatformChatOptions.builder().build();
        try {
            PropertyDescriptorUtils.copyPropertiesIgnoreNull(this, options);
        } catch (Exception e) {
            throw new RuntimeException("Failed to copy properties", e);
        }
        Optional.ofNullable(this.dashScopeOptions).ifPresent(item ->
                options.dashScopeOptions = (DashScopeChatOptions) item.copy());
        Optional.ofNullable(this.deepSeekOptions).ifPresent(item ->
                options.deepSeekOptions = item.copy());
        Optional.ofNullable(this.ollamaOptions).ifPresent(item ->
                options.ollamaOptions = item.copy());
        return options;
    }

    public static final class Builder implements ToolCallingChatOptions.Builder {

        private final PlatformChatOptions options = new PlatformChatOptions();

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

        /**
         * 设置DeepSeek配置
         *
         * @param deepSeekOptions DeepSeek配置
         * @return Builder
         * @since 1.0.0
         */
        public @NonNull Builder deepSeekOptions(@NonNull DeepSeekChatOptions deepSeekOptions) {
            this.options.setDeepSeekOptions(deepSeekOptions);
            return this;
        }

        /**
         * 设置DashScope配置
         *
         * @param dashScopeOptions DashScope配置
         * @return Builder
         * @since 1.0.0
         */
        public @NonNull Builder dashScopeOptions(@NonNull DashScopeChatOptions dashScopeOptions) {
            this.options.setDashScopeOptions(dashScopeOptions);
            return this;
        }

        /**
         * 设置Ollama配置
         *
         * @param ollamaOptions Ollama配置
         * @return Builder
         * @since 1.0.0
         */
        public @NonNull Builder ollamaOptions(@NonNull OllamaChatOptions ollamaOptions) {
            this.options.setOllamaOptions(ollamaOptions);
            return this;
        }

        /**
         * 设置模型元数据
         *
         * @param metaData 模型元数据
         * @return Builder
         * @since 1.0.0
         */
        public @NonNull Builder metaData(@Nullable ChatModelMetaDataVO metaData) {
            this.options.setMetaData(metaData);
            return this;
        }

        @Override
        public @NonNull Builder toolCallbacks(@NonNull List<ToolCallback> toolCallbacks) {
            this.options.setToolCallbacks(toolCallbacks);
            return this;
        }

        @Override
        public @NonNull Builder toolCallbacks(ToolCallback @NonNull ... toolCallbacks) {
            Assert.notNull(toolCallbacks, "toolCallbacks cannot be null");
            this.options.setToolCallbacks(Arrays.asList(toolCallbacks));
            return this;
        }

        @Override
        public @NonNull Builder toolNames(@NonNull Set<String> toolNames) {
            this.options.setToolNames(toolNames);
            return this;
        }

        @Override
        public @NonNull Builder toolNames(String @NonNull ... toolNames) {
            Assert.notNull(toolNames, "toolNames cannot be null");
            this.options.setToolNames(Set.of(toolNames));
            return this;
        }

        @Override
        public @NonNull Builder toolContext(@NonNull Map<String, Object> context) {
            this.options.setToolContext(context);
            return this;
        }

        @Override
        public @NonNull Builder toolContext(@NonNull String key, @NonNull Object value) {
            Assert.hasText(key, "key cannot be null");
            Assert.notNull(value, "value cannot be null");
            Map<String, Object> updatedToolContext = new HashMap<>(this.options.getToolContext());
            updatedToolContext.put(key, value);
            this.options.setToolContext(updatedToolContext);
            return this;
        }

        @Override
        public @NonNull Builder internalToolExecutionEnabled(
                @Nullable Boolean internalToolExecutionEnabled) {
            this.options.setInternalToolExecutionEnabled(internalToolExecutionEnabled);
            return this;
        }

        @Override
        public @NonNull Builder model(@Nullable String model) {
            this.options.setModel(model);
            return this;
        }

        @Override
        public @NonNull Builder frequencyPenalty(@Nullable Double frequencyPenalty) {
            this.options.setFrequencyPenalty(frequencyPenalty);
            return this;
        }

        @Override
        public @NonNull Builder maxTokens(@Nullable Integer maxTokens) {
            this.options.setMaxTokens(maxTokens);
            return this;
        }

        @Override
        public @NonNull Builder presencePenalty(@Nullable Double presencePenalty) {
            this.options.setPresencePenalty(presencePenalty);
            return this;
        }

        @Override
        public @NonNull Builder stopSequences(@Nullable List<String> stopSequences) {
            this.options.setStopSequences(stopSequences);
            return this;
        }

        @Override
        public @NonNull Builder temperature(@Nullable Double temperature) {
            this.options.setTemperature(temperature);
            return this;
        }

        @Override
        public @NonNull Builder topK(@Nullable Integer topK) {
            this.options.setTopK(topK);
            return this;
        }

        @Override
        public @NonNull Builder topP(@Nullable Double topP) {
            this.options.setTopP(topP);
            return this;
        }

        public @NonNull Builder enableSearch(@Nullable Boolean enableSearch) {
            this.options.setEnableSearch(enableSearch);
            return this;
        }

        public @NonNull Builder enableThinking(@Nullable Boolean enableThinking) {
            this.options.setEnableThinking(enableThinking);
            return this;
        }

        @Override
        public @NonNull PlatformChatOptions build() {
            DashScopeChatOptions dashScopeOptions = Optional.ofNullable(this.options.dashScopeOptions)
                    .orElseGet(() -> DashScopeChatOptions.builder().build());
            DeepSeekChatOptions deepSeekOptions = Optional.ofNullable(this.options.deepSeekOptions)
                    .orElseGet(() -> DeepSeekChatOptions.builder().build());
            OllamaChatOptions ollamaOptions = Optional.ofNullable(this.options.ollamaOptions)
                    .orElseGet(() -> OllamaChatOptions.builder().build());

            PropertyDescriptorUtils.copyPropertiesIgnoreNull(this.options, dashScopeOptions);
            PropertyDescriptorUtils.copyPropertiesIgnoreNull(this.options, deepSeekOptions);
            PropertyDescriptorUtils.copyPropertiesIgnoreNull(this.options, ollamaOptions);

            if (this.options.enableSearch != null) {
                dashScopeOptions.setEnableSearch(this.options.enableSearch);
            }
            if (this.options.enableThinking != null) {
                dashScopeOptions.setEnableThinking(this.options.enableThinking);
                ollamaOptions.setThinkOption(ThinkOption.ThinkBoolean.ENABLED);
            }
            if (this.options.metaData != null) {
                dashScopeOptions.setMultiModel(Optional.ofNullable(this.options.metaData.getRequestModalities())
                        .map(modalities -> modalities.size() > 1)
                        .orElse(null));
            }
            this.options.dashScopeOptions = dashScopeOptions;
            this.options.deepSeekOptions = deepSeekOptions;
            this.options.ollamaOptions = ollamaOptions;
            return this.options;
        }
    }
}
