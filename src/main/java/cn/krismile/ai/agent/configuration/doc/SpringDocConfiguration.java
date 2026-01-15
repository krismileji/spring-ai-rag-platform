package cn.krismile.ai.agent.configuration.doc;

import com.fasterxml.jackson.databind.JavaType;
import host.springboot.framework3.core.enumeration.BaseEnum;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.ParameterCustomizer;
import org.springdoc.core.customizers.PropertyCustomizer;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * SpringDoc Configuration
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Configuration
public class SpringDocConfiguration {

    static {
        SpringDocUtils.getConfig()
                .replaceWithSchema(Long.class, new StringSchema())
                .replaceWithSchema(long.class, new StringSchema());
    }

    @Bean
    @SuppressWarnings("unchecked")
    public PropertyCustomizer propertyCustomizer() {
        return (property, annotatedType) -> {
            Type type = annotatedType.getType();
            Class<?> rawClass = type instanceof JavaType javaType
                    ? javaType.getRawClass()
                    : ResolvableType.forType(type).resolve();
            if (rawClass == null || !rawClass.isEnum() || !BaseEnum.class.isAssignableFrom(rawClass)) {
                return property;
            }
            property._enum(this.parseEnums(rawClass));
            return property;
        };
    }

    @Bean
    @SuppressWarnings({"unchecked"})
    public ParameterCustomizer enumParameterCustomizer() {
        return (Parameter parameter, MethodParameter methodParameter) -> {
            Class<?> rawClass = methodParameter.getParameterType();
            if (!rawClass.isEnum() || !BaseEnum.class.isAssignableFrom(rawClass)) {
                return parameter;
            }
            Optional.ofNullable(parameter.getSchema()).ifPresent(schema ->
                    schema._enum(this.parseEnums(rawClass)));
            return parameter;
        };
    }

    private List<String> parseEnums(Class<?> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants())
                .map(e -> (BaseEnum<?>) e)
                .map(e -> e.getValue() + "-" + e.getReasonPhrase())
                .toList();
    }
}
