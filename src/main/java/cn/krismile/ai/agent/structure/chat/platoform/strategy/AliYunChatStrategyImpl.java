package cn.krismile.ai.agent.structure.chat.platoform.strategy;

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
    protected Flux<ChatModelVO> queryModels() {
        return webClientBuilder.baseUrl("https://bailian-cs.console.aliyun.com")
                .build()
                .post()
                .uri("/data/api.json")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED.toString())
                .body(BodyInserters.fromFormData("region", "cn-beijing")
                        .with("params", """
                                {
                                	"Api": "zeldaHttp.dashscopeModel./zelda/api/v1/modelCenter/listFoundationModels",
                                	"V": "1.0",
                                	"Data": {
                                		"input": {
                                			"pageNo": 1,
                                			"pageSize": 60,
                                			"group": true,
                                			"capabilities": [
                                				"TG"
                                			]
                                		},
                                		"cornerstoneParam": {
                                			"feURL": "https://bailian.console.aliyun.com/cn-beijing/?tab=model#/model-market/all"
                                		}
                                	}
                                }
                                """))
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
                                .setPlatform(this.platform().getValue())
                                .setPlatformName(this.platform().getReasonPhrase())
                                .setModel(model.get("model").asText())
                                .setModelName(model.get("name").asText())
                                .setDescription(model.get("description").asText())
                        )))
                        .orElseGet(Flux::empty)
                );
    }
}