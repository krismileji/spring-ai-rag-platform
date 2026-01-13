package cn.krismile.ai.agent;

import cn.krismile.ai.agent.util.ObjectMapperUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import host.springboot.framework.context.advice.annotation.EnableGlobalControllerAdvice;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@EnableGlobalControllerAdvice
public class AiAgentApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(AiAgentApplication.class, args);
        ObjectMapperUtils.setObjectMapper(context.getBean(ObjectMapper.class));
    }
}
