package cn.krismile.ai.agent.controller.knowledge;

import cn.krismile.ai.agent.model.request.knowledge.KnowledgeAddEditRequest;
import cn.krismile.ai.agent.model.response.knowledge.KnowledgeVO;
import cn.krismile.ai.agent.structure.chat.knowledge.service.KnowledgeService;
import host.springboot.framework3.core.response.R;
import host.springboot.framework3.core.response.vo.VO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;
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

    /**
     * 校验知识库名称是否可用
     *
     * @param name 知识库名称
     * @return 校验结果信息
     * @since 1.0.0
     */
    @Operation(summary = "校验名称")
    @GetMapping("/checkName")
    public Mono<VO<String>> checkName(@RequestParam String name) {
        return knowledgeService.checkName(name).map(R::data);
    }

    /**
     * 查询知识库列表
     *
     * @return 知识库列表
     * @since 1.0.0
     */
    @Operation(summary = "查询知识库列表")
    @GetMapping("/list")
    public Mono<VO<List<KnowledgeVO>>> list() {
        return knowledgeService.list().collectList().map(R::data);
    }

    /**
     * 新增或编辑知识库
     *
     * @param request 新增/编辑请求参数
     * @return 是否操作成功
     * @since 1.0.0
     */
    @Operation(summary = "新增/编辑知识库")
    @PostMapping("/addEdit")
    public Mono<VO<Boolean>> addEdit(@RequestBody @Valid KnowledgeAddEditRequest request) {
        return knowledgeService.addEdit(request).map(R::data);
    }
}
