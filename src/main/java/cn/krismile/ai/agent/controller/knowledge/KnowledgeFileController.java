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

    /**
     * 查询文件列表
     *
     * @param knowledgeId 知识库ID
     * @return 文件列表
     * @since 1.0.0
     */
    @Operation(summary = "查询文件列表", parameters = {
            @Parameter(name = "knowledgeId", description = "知识库 ID", schema = @Schema(type = "string"))
    })
    @GetMapping("/list/{knowledgeId}")
    public Mono<VO<List<KnowledgeFileVO>>> list(@PathVariable Long knowledgeId) {
        return this.fileService.list(knowledgeId).collectList().map(R::data);
    }

    /**
     * 查询文件详情
     *
     * @param fileId 文件ID
     * @return 文件详情列表
     * @since 1.0.0
     */
    @Operation(summary = "查询文件详情", parameters = {
            @Parameter(name = "fileId", description = "文件 ID", schema = @Schema(type = "string"))
    })
    @GetMapping("/listFileDetails/{fileId}")
    public Mono<VO<List<KnowledgeFileDetailVO>>> listFileDetails(@PathVariable Long fileId) {
        return this.fileService.listFileDetails(fileId).collectList().map(R::data);
    }

    /**
     * 上传文件
     *
     * @param knowledgeId 知识库ID
     * @param files 文件流
     * @return 上传结果列表
     * @since 1.0.0
     */
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

    /**
     * 保存文件
     *
     * @param knowledgeId 知识库ID
     * @param requests 文件保存请求列表
     * @return 保存后的文件ID列表
     * @since 1.0.0
     */
    @Operation(summary = "保存文件", parameters = {
            @Parameter(name = "knowledgeId", description = "知识库 ID", schema = @Schema(type = "string"))
    })
    @PutMapping("/add/{knowledgeId}")
    public Mono<VO<List<Long>>> add(
            @PathVariable Long knowledgeId,
            @RequestBody Flux<KnowledgeFileAddRequest> requests) {
        return this.fileService.add(knowledgeId, requests).collectList().map(R::data);
    }

    /**
     * 删除文件
     *
     * @param fileId 文件ID
     * @return 是否删除成功
     * @since 1.0.0
     */
    @Operation(summary = "删除文件", parameters = {
            @Parameter(name = "fileId", description = "文件 ID", schema = @Schema(type = "string"))
    })
    @DeleteMapping("/del/{fileId}")
    public Mono<VO<Boolean>> del(@PathVariable Long fileId) {
        return this.fileService.del(fileId).map(R::data);
    }
}
