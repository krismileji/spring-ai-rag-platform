package cn.krismile.ai.agent.controller;

import host.springboot.framework3.core.response.R;
import host.springboot.framework3.core.response.vo.VO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * DemoController
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@RestController
public class DemoController {

    @GetMapping("/demo")
    public VO<?> demo() {
        return R.ok();
    }
}
