package cn.krismile.ai.agent.configuration.database;

import cn.krismile.ai.agent.configuration.security.context.SecurityUtils;
import cn.krismile.ai.agent.model.domain.BaseIdDO;
import cn.krismile.ai.agent.util.SnowflakeIdGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.DialectResolver;
import org.springframework.data.r2dbc.dialect.R2dbcDialect;
import org.springframework.data.r2dbc.mapping.event.BeforeConvertCallback;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableR2dbcAuditing
@EnableR2dbcRepositories(basePackages = "cn.krismile.ai.agent.repository")
public class R2dbcConfiguration {

    @Bean
    public SnowflakeIdGenerator snowflakeIdGenerator() {
        return new SnowflakeIdGenerator(1);
    }

    @Bean
    public BeforeConvertCallback<BaseIdDO> idGeneratorCallback(SnowflakeIdGenerator snowflakeIdGenerator) {
        return (entity, table) -> {
            if (entity.getId() == null) {
                entity.setId(snowflakeIdGenerator.nextId());
            }
            return Mono.just(entity);
        };
    }

    /**
     * 审计员感知
     *
     * @return ReactiveAuditorAware
     * @since 1.0.0
     */
    @Bean
    public ReactiveAuditorAware<Long> auditorAware() {
        return () -> SecurityUtils.getUserId().onErrorResume(e -> Mono.just(-1L));
    }

    /**
     * R2DBC 自定义转换器
     *
     * @param objectMapper      Jackson ObjectMapper
     * @param connectionFactory ConnectionFactory
     * @return R2dbcCustomConversions
     * @since 1.0.0
     */
    @Bean
    public R2dbcCustomConversions r2dbcCustomConversions(ObjectMapper objectMapper, ConnectionFactory connectionFactory) {
        R2dbcDialect dialect = DialectResolver.getDialect(connectionFactory);
        List<Object> converters = new ArrayList<>(dialect.getConverters());
        converters.addAll(R2dbcCustomConversions.STORE_CONVERTERS);
        // 注册 WritingConverter: JsonType -> String
        converters.add(new R2dbcJacksonConverters.JsonTypeToJsonConverter(objectMapper));
        // 注册 ReadingConverter: String -> JsonType
        converters.add(new R2dbcJacksonConverters.JsonToJsonTypeConverterFactory(objectMapper));
        // 注册 WritingConverter: JsonNode -> String
        converters.add(new R2dbcJacksonConverters.JsonNodeToJsonConverter(objectMapper));
        // 注册 ReadingConverter: String -> JsonNode
        converters.add(new R2dbcJacksonConverters.JsonToJsonNodeConverterFactory(objectMapper));
        return new R2dbcCustomConversions(
                CustomConversions.StoreConversions.of(dialect.getSimpleTypeHolder()),
                converters
        );
    }
}
