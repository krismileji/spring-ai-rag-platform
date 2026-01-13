package cn.krismile.ai.agent.repository.platform;

import cn.krismile.ai.agent.mapper.AiPlatformMapper;
import cn.krismile.ai.agent.model.domain.AiPlatformDO;
import cn.krismile.ai.agent.model.enumeration.chat.ChatPlatformEnum;
import cn.krismile.ai.agent.structure.chat.model.ChatPlatformDTO;
import cn.krismile.ai.agent.structure.chat.platoform.PlatformService;
import cn.krismile.ai.agent.util.AESEncryptionUtil;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import host.springboot.framework3.core.enumeration.error.ErrorCodeEnum;
import host.springboot.framework3.core.exception.ApplicationException;
import org.springframework.stereotype.Service;

import static cn.krismile.ai.agent.model.domain.table.AiPlatformDOTableDef.AI_PLATFORM_DO;

/**
 * AiPlatformRepositoryImpl
 *
 * @author Auto Generated
 * @since 1.0.0
 */
@Service
public class AiPlatformRepositoryImpl extends ServiceImpl<AiPlatformMapper, AiPlatformDO> implements AiPlatformRepository {
}