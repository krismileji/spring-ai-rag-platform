package cn.krismile.ai.agent;

import cn.krismile.ai.agent.model.enumeration.chat.ChatPlatformEnum;
import cn.krismile.ai.agent.structure.chat.chatmodel.factory.ChatModelFactory;
import cn.krismile.ai.agent.structure.chat.chatmodel.factory.options.PlatformChatOptions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class AiAgentApplicationTests {

    // @Resource
    // private DashScopeChatModel dashScopeChatModel;
    // @Resource
    // private DashScopeChatProperties chatProperties;
    // @Resource
    // private AliyunPlatformModelBuilder aliyunPlatformModelBuilder;

    @Test
    void contextLoads() {
        CountDownLatch latch = new CountDownLatch(1);
        ChatModel chatModel = ChatModelFactory.builder(ChatPlatformEnum.ALIYUN).chat("qwen-plus", PlatformChatOptions.builder()
                .enableThinking(true)
                .build());
        // ChatModel chatModel = aliyunPlatformModelBuilder.create(PlatformChatOptions.builder().dashScopeOptions(chatProperties.getOptions()).build());

        System.out.println("\n" + "=".repeat(20) + "思考过程" + "=".repeat(20) + "\n");

        chatModel.stream(new Prompt("计算 25 * 4 + 10"))
                .subscribe(response -> {
                    // 打印思考过程
                    Object reasoningContent = response.getMetadata().get("reasoning_content");
                    if (reasoningContent != null) {
                        System.out.print(reasoningContent);
                    }

                    // 打印最终回复
                    String content = response.getResult().getOutput().getText();
                    if (content != null && !content.isEmpty()) {
                        if (reasoningContent != null) {
                            System.out.println("\n" + "=".repeat(20) + "完整回复" + "=".repeat(20) + "\n");
                        }
                        System.out.print(content);
                    }
                }, error -> {
                    System.err.println("错误: " + error.getMessage());
                    latch.countDown();
                }, latch::countDown);

        try {
            latch.await(60, TimeUnit.SECONDS); // 等待60秒
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
