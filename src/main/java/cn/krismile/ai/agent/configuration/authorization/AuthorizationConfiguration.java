package cn.krismile.ai.agent.configuration.authorization;

import cn.dev33.satoken.context.SaTokenContextForThreadLocalStaff;
import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.listener.SaTokenListener;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpLogic;
import cn.krismile.ai.agent.structure.authorization.SaTokenLogListener;
import cn.krismile.ai.agent.structure.authorization.StpInterfaceImpl;
import com.alibaba.ttl.TransmittableThreadLocal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * AuthorizationConfiguration
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Configuration
public class AuthorizationConfiguration {

    static {
        SaTokenContextForThreadLocalStaff.modelBoxThreadLocal = new TransmittableThreadLocal<>();
    }

    @Bean
    public StpLogic getStpLogicJwt() {
        return new StpLogicJwtForSimple();
    }

    @Bean
    public StpInterface stpInterface() {
        return new StpInterfaceImpl();
    }

    @Bean
    public SaTokenListener saTokenLogListener() {
        return new SaTokenLogListener();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
