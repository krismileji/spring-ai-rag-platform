package cn.krismile.ai.agent.structure.chat.platoform.service;

import cn.krismile.ai.agent.model.domain.AiPlatformDO;
import cn.krismile.ai.agent.model.enumeration.chat.ChatPlatformEnum;
import cn.krismile.ai.agent.model.request.chat.ChatModelEditRequest;
import cn.krismile.ai.agent.model.request.chat.ChatOptionsRequest;
import cn.krismile.ai.agent.model.request.chat.ChatPlatformEditRequest;
import cn.krismile.ai.agent.model.response.chat.ChatModelVO;
import cn.krismile.ai.agent.model.response.chat.ChatPlatformVO;
import cn.krismile.ai.agent.repository.platform.AiModelRepository;
import cn.krismile.ai.agent.repository.platform.AiPlatformRepository;
import cn.krismile.ai.agent.structure.chat.platoform.strategy.AbstractChatPlatformStrategy;
import cn.krismile.ai.agent.util.AESEncryptionUtil;
import host.springboot.framework3.core.enumeration.error.ErrorCodeEnum;
import host.springboot.framework3.core.exception.ApplicationException;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

/**
 * 平台服务实现
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Service
public class PlatformServiceImpl implements PlatformService {

    @Resource
    private AiPlatformRepository aiPlatformRepository;
    @Resource
    private AiModelRepository aiModelRepository;
    @Resource
    private ReactiveRedisTemplate<String, ChatModelVO> reactiveRedisTemplate;

    @Override
    public Mono<List<ChatPlatformVO>> listPlatforms() {
        return aiPlatformRepository.findAll()
                .collectList()
                .map(platforms -> platforms.stream()
                        .map(ChatPlatformVO::of)
                        .toList());
    }

    @Override
    public Flux<ChatModelVO> listModels() {
        return aiPlatformRepository.findByEnabled(true)
                .flatMap(platform -> platform.getPlatform().strategy().listAllModels());
    }

    @Override
    public Mono<Boolean> editPlatform(ChatPlatformEditRequest request) {
        ChatPlatformEnum platform = request.getPlatform();
        String apiKey = request.getApiKey();
        ChatOptionsRequest defaultOptions = request.getDefaultOptions();
        Boolean enabled = request.getEnabled();

        String encodedApiKey = StringUtils.isNotBlank(apiKey) ? AESEncryptionUtil.encrypt(apiKey, SECRET_KEY) : null;

        return aiPlatformRepository.findByPlatform(platform)
                .switchIfEmpty(Mono.defer(() -> {
                    AiPlatformDO newPlatform = new AiPlatformDO();
                    newPlatform.setPlatform(platform);
                    return Mono.just(newPlatform);
                }))
                .flatMap(p -> {
                    Optional.ofNullable(encodedApiKey).ifPresent(p::setApiKey);
                    Optional.ofNullable(defaultOptions).ifPresent(p::setDefaultOptions);
                    p.setEnabled(enabled);
                    return aiPlatformRepository.save(p);
                })
                .switchIfEmpty(Mono.error(new ApplicationException(
                        ErrorCodeEnum.DATABASE_SERVICE_ERROR, "Failed to edit platform")))
                .thenReturn(true);
    }

    @Override
    public Mono<Boolean> editModel(ChatModelEditRequest request) {
        Long id = request.getId();
        String model = request.getModel();
        Boolean enabled = request.getEnabled();

        return aiModelRepository.findById(id)
                .filter(m -> m.getCode().equals(model))
                .flatMap(m -> aiModelRepository.save(m.setEnabled(enabled))
                        .flatMap(savedModel -> aiPlatformRepository.findById(savedModel.getRelPlatformId())
                                .flatMap(platform -> {
                                    ChatPlatformEnum platformEnum = platform.getPlatform();
                                    String cacheKey = AbstractChatPlatformStrategy.REDIS_MODEL_CACHE_KEY.apply(platformEnum);

                                    ChatModelVO vo = new ChatModelVO()
                                            .setId(savedModel.getId())
                                            .setPlatform(platformEnum.getValue())
                                            .setPlatformName(platformEnum.getReasonPhrase())
                                            .setModel(savedModel.getCode())
                                            .setModelName(savedModel.getName())
                                            .setDescription(savedModel.getDescription())
                                            .setEnabled(savedModel.getEnabled())
                                            .setSort(savedModel.getSort());

                                    return reactiveRedisTemplate.opsForHash()
                                            .put(cacheKey, vo.getModel(), vo)
                                            .thenReturn(savedModel);
                                })))
                .switchIfEmpty(Mono.error(new ApplicationException(
                        ErrorCodeEnum.DATABASE_SERVICE_ERROR, "Failed to update model")))
                .thenReturn(true);
    }
}
