package cn.krismile.ai.agent.controller.knowledge;

import cn.krismile.ai.agent.model.request.knowledge.KnowledgeAddEditRequest;
import cn.krismile.ai.agent.model.response.knowledge.KnowledgeVO;
import cn.krismile.ai.agent.service.knowledge.KnowledgeService;
import host.springboot.framework3.core.response.R;
import host.springboot.framework3.core.response.vo.VO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 知识库管理
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@RestController
@RequestMapping("/knowledge")
@Tag(name = "知识库管理", description = "知识库管理")
public class KnowledgeController {

    @Resource
    private KnowledgeService knowledgeService;

    @Operation(summary = "校验名称")
    @GetMapping("/checkName")
    public VO<String> checkName(@RequestParam String name) {
        return R.data(this.knowledgeService.checkName(name));
    }

    @Operation(summary = "查询知识库列表")
    @GetMapping("/list")
    public VO<List<KnowledgeVO>> list() {
        return R.data(this.knowledgeService.list());
    }

    @Operation(summary = "新增/编辑知识库")
    @PostMapping("/addEdit")
    public VO<Boolean> addEdit(@RequestBody @Valid KnowledgeAddEditRequest request) {
        return R.data(this.knowledgeService.addEdit(request));
    }
}
