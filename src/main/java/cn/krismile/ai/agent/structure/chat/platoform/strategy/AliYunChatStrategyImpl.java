package cn.krismile.ai.agent.structure.chat.platoform.strategy;

import cn.krismile.ai.agent.model.enumeration.chat.ChatModelTypeEnum;
import cn.krismile.ai.agent.model.enumeration.chat.ChatPlatformEnum;
import cn.krismile.ai.agent.model.response.chat.ChatModelVO;
import cn.krismile.ai.agent.structure.chat.platoform.ChatPlatformStrategy;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 阿里云聊天平台策略实现
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Component(AliYunChatStrategyImpl.BEAN_NAME)
public class AliYunChatStrategyImpl extends AbstractChatPlatformStrategy implements ChatPlatformStrategy {

    public static final String BEAN_NAME = "aliYunPlatformStrategyImpl";

    @Resource
    private WebClient.Builder webClientBuilder;

    @Override
    public ChatPlatformEnum platform() {
        return ChatPlatformEnum.ALIYUN;
    }

    @Override
    protected Flux<ChatModelVO> queryModels(ChatModelTypeEnum type) {
        AtomicInteger modelIndex = new AtomicInteger(0);
        return webClientBuilder.baseUrl("https://bailian-cs.console.aliyun.com")
                .build()
                .post()
                .uri("/data/api.json")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED.toString())
                .body(BodyInserters.fromFormData("region", "cn-beijing")
                        .with("params", this.parseParams(type)))
                .retrieve()
                .bodyToMono(ObjectNode.class)
                .filter(objectNode -> objectNode.optional("code")
                        .filter(node -> StringUtils.equals(node.asText(""), "200"))
                        .isPresent())
                .flatMapMany(objectNode -> objectNode.optional("data")
                        .filter(dataNode -> dataNode.optional("httpStatus")
                                .filter(node -> node.asInt() == 200)
                                .isPresent())
                        .flatMap(dataNode -> dataNode.optional("DataV2"))
                        .flatMap(node -> node.optional("data"))
                        .filter(dataNode -> dataNode.optional("code")
                                .filter(node -> StringUtils.equals(node.asText(""), "200"))
                                .isPresent())
                        .flatMap(node -> node.optional("data"))
                        .flatMap(node -> node.optional("list"))
                        .map(JsonNode::elements)
                        .map(iterator -> {
                            List<JsonNode> modes = new ArrayList<>();
                            while (iterator.hasNext()) {
                                iterator.next().optional("items")
                                        .map(JsonNode::elements)
                                        .flatMap(elements -> elements.hasNext()
                                                ? Optional.of(elements.next())
                                                : Optional.empty()
                                        )
                                        .filter(element -> element.has("model")
                                                && element.has("name")
                                                && element.has("description"))
                                        .ifPresent(modes::add);
                            }
                            return modes;
                        })
                        .map(Collection::stream)
                        .map(modelStream -> Flux.fromStream(modelStream.map(model -> new ChatModelVO()
                                .setType(type)
                                .setPlatform(this.platform().getValue())
                                .setPlatformName(this.platform().getReasonPhrase())
                                .setModel(model.get("model").asText())
                                .setModelName(model.get("name").asText())
                                .setDescription(model.get("description").asText())
                                .setEnabled(true)
                                .setSort(modelIndex.getAndIncrement())
                        )))
                        .orElseGet(Flux::empty)
                );
    }

    /**
     * 解析参数
     *
     * @param type 模型类型
     * @return 参数
     * @since 1.0.0
     */
    private String parseParams(ChatModelTypeEnum type) {
        String capability = switch (type) {
            case CHAT -> "TG";
            case EMBEDDING -> "TR";
            default -> throw new IllegalArgumentException("Invalid type: " + type);
        };
        return """
                {
                  "Api": "zeldaHttp.dashscopeModel./zelda/api/v1/modelCenter/listFoundationModels",
                  "V": "1.0",
                  "Data": {
                    "input": {
                      "pageNo": 1,
                      "pageSize": 60,
                      "group": true,
                      "capabilities": [
                        "%s"
                      ]
                    },
                    "cornerstoneParam": {
                      "feURL": "https://bailian.console.aliyun.com/cn-beijing/?tab=model#/model-market/all"
                    }
                  }
                }
                """.formatted(capability);
    }
}