package cn.krismile.ai.agent.configuration;

import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

/**
 * WebClient 配置类
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Configuration
public class WebClientConfiguration {

    /**
     * WebClient自定义配置
     *
     * @return WebClient自定义器
     * @since 1.0.0
     */
    @Bean
    public WebClientCustomizer webClientCustomizer() {
        return builder -> builder
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
                        .responseTimeout(Duration.ofMinutes(1))))
                .codecs(configurer -> configurer.defaultCodecs()
                        .maxInMemorySize(1024 * 1024 * 10));
    }
}
