package cn.krismile.ai.agent.structure.chat.chatmodel.factory.context;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.observation.ChatModelObservationConvention;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionEligibilityPredicate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 聊天模型公共上下文，封装各平台共享的依赖。
 *
 * @param retryTemplate 重试模板
 * @param toolCallingManager 工具调用管理器
 * @param responseErrorHandler 响应错误处理器
 * @param observationRegistry 观测注册器
 * @param webClientBuilderProvider WebClient.Builder 提供者
 * @param restClientBuilderProvider RestClient.Builder 提供者
 * @param observationConvention 观测约定
 * @param toolExecutionEligibilityPredicate 工具执行资格判断
 * @author JiYinchuan
 * @since 1.0.0
 */
@Component
public record CommonChatModelContext(
        RetryTemplate retryTemplate,
        ToolCallingManager toolCallingManager,
        ResponseErrorHandler responseErrorHandler,
        ObjectProvider<ObservationRegistry> observationRegistry,
        ObjectProvider<WebClient.Builder> webClientBuilderProvider,
        ObjectProvider<RestClient.Builder> restClientBuilderProvider,
        ObjectProvider<ChatModelObservationConvention> observationConvention,
        ObjectProvider<ToolExecutionEligibilityPredicate> toolExecutionEligibilityPredicate
) {
}
