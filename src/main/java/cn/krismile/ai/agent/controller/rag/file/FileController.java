// package cn.krismile.ai.agent.controller.rag.file;
//
// import cn.krismile.ai.agent.structure.rag.file.service.FileService;
// import host.springboot.framework3.core.response.R;
// import host.springboot.framework3.core.response.vo.VO;
// import io.swagger.v3.oas.annotations.Operation;
// import io.swagger.v3.oas.annotations.tags.Tag;
// import jakarta.annotation.Resource;
// import org.springframework.http.codec.multipart.FilePart;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestPart;
// import org.springframework.web.bind.annotation.RestController;
// import reactor.core.publisher.Flux;
// import reactor.core.publisher.Mono;
//
// /**
//  * 文件管理
//  *
//  * @author JiYinchuan
//  * @since 1.0.0
//  */
// @RestController
// @RequestMapping("/rag/file")
// @Tag(name = "文件管理", description = "文件管理")
// public class FileController {
//
//     @Resource
//     private FileService fileService;
//
//     @Operation(summary = "上传文件")
//     @PostMapping("/uploadFile")
//     public Mono<VO<Boolean>> uploadFile(@RequestPart Flux<FilePart> files) {
//         return this.fileService.uploads(files)
//                 .collectList()
//                 .map(list -> !list.isEmpty())
//                 .map(R::data);
//     }
// }