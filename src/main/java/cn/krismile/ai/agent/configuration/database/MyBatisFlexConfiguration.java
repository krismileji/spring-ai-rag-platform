package cn.krismile.ai.agent.configuration.database;

import cn.dev33.satoken.stp.StpUtil;
import cn.krismile.ai.agent.context.RequestContext;
import cn.krismile.ai.agent.model.domain.BaseDO;
import com.mybatisflex.annotation.AbstractInsertListener;
import com.mybatisflex.annotation.AbstractUpdateListener;
import com.mybatisflex.core.FlexGlobalConfig;
import com.mybatisflex.core.audit.AuditManager;
import com.mybatisflex.spring.boot.MyBatisFlexCustomizer;
import host.springboot.framework3.core.logging.LoggingComponent;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * MyBatisFlexConfiguration
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Configuration
public class MyBatisFlexConfiguration implements MyBatisFlexCustomizer, LoggingComponent {

    public static final Logger LOGGER = LoggerFactory.getLogger(MyBatisFlexConfiguration.class);

    public MyBatisFlexConfiguration() {
        AuditManager.setAuditEnable(true);
        AuditManager.setMessageCollector(auditMessage -> LOGGER.debug(
                "{},{}ms", auditMessage.getFullSql(), auditMessage.getElapsedTime()));
    }

    @Override
    public void customize(FlexGlobalConfig flexGlobalConfig) {
        // noinspection rawtypes
        flexGlobalConfig.registerInsertListener(new AbstractInsertListener<BaseDO>() {
            @Override
            public void doInsert(BaseDO entity) {
                Long userId = RequestContext.syncApply(() -> StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null);
                entity.setCreateTime(LocalDateTime.now());
                entity.setCreateUser(userId);
                entity.setUpdateTime(LocalDateTime.now());
                entity.setUpdateUser(userId);
            }
        }, BaseDO.class);
        // noinspection rawtypes
        flexGlobalConfig.registerUpdateListener(new AbstractUpdateListener<BaseDO>() {
            @Override
            public void doUpdate(BaseDO entity) {
                Long userId = RequestContext.syncApply(() -> StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null);
                entity.setUpdateTime(LocalDateTime.now());
                entity.setUpdateUser(userId);
            }
        }, BaseDO.class);
    }

    @Override
    public @NonNull String logTag() {
        return "MybatisFlex";
    }
}
