package cn.krismile.ai.agent.controller.knowledge;

import cn.krismile.ai.agent.model.request.knowledge.KnowledgeFileAddRequest;
import cn.krismile.ai.agent.model.response.knowledge.KnowledgeFileDetailVO;
import cn.krismile.ai.agent.model.response.knowledge.KnowledgeFileUploadVO;
import cn.krismile.ai.agent.model.response.knowledge.KnowledgeFileVO;
import cn.krismile.ai.agent.structure.rag.file.service.FileService;
import host.springboot.framework3.core.response.R;
import host.springboot.framework3.core.response.vo.VO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 知识库文件管理
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@RestController
@RequestMapping("/knowledge/file")
@Tag(name = "知识库文件管理", description = "知识库文件管理")
public class KnowledgeFileController {

    @Resource
    private FileService fileService;

    @Operation(summary = "查询文件列表", parameters = {
            @Parameter(name = "knowledgeId", description = "知识库 ID", schema = @Schema(type = "string"))
    })
    @GetMapping("/list/{knowledgeId}")
    public VO<List<KnowledgeFileVO>> list(@PathVariable Long knowledgeId) {
        return R.data(this.fileService.list(knowledgeId));
    }

    @Operation(summary = "查询文件详情", parameters = {
            @Parameter(name = "fileId", description = "文件 ID", schema = @Schema(type = "string"))
    })
    @GetMapping("/listFileDetails/{fileId}")
    public VO<List<KnowledgeFileDetailVO>> listFileDetails(@PathVariable Long fileId) {
        return R.data(this.fileService.listFileDetails(fileId));
    }

    @Operation(summary = "上传文件", parameters = {
            @Parameter(name = "knowledgeId", description = "知识库 ID", schema = @Schema(type = "string")),
            @Parameter(name = "files", description = "文件", schema = @Schema(type = "file"), example = "文件内容")
    })
    @PostMapping("/uploadFile/{knowledgeId}")
    public Mono<VO<List<KnowledgeFileUploadVO>>> uploadFile(
            @PathVariable Long knowledgeId,
            @RequestPart Flux<FilePart> files) {
        return this.fileService.uploads(knowledgeId, files).collectList().map(R::data);
    }

    @Operation(summary = "保存文件", parameters = {
            @Parameter(name = "knowledgeId", description = "知识库 ID", schema = @Schema(type = "string"))
    })
    @PutMapping("/add/{knowledgeId}")
    public Mono<VO<List<Long>>> add(
            @PathVariable Long knowledgeId,
            @RequestBody Flux<KnowledgeFileAddRequest> requests) {
        return this.fileService.add(knowledgeId, requests).collectList().map(R::data);
    }

    @Operation(summary = "删除文件", parameters = {
            @Parameter(name = "fileId", description = "文件 ID", schema = @Schema(type = "string"))
    })
    @DeleteMapping("/del/{fileId}")
    public VO<Boolean> del(@PathVariable Long fileId) {
        return R.data(this.fileService.del(fileId));
    }
}
