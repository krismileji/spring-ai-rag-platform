package cn.krismile.ai.agent.structure.rag.file;

import cn.krismile.ai.agent.structure.rag.file.parser.JsonFileParser;
import cn.krismile.ai.agent.structure.rag.file.parser.MarkdownFileParser;
import cn.krismile.ai.agent.structure.rag.file.parser.PdfFileParser;
import com.alibaba.cloud.ai.transformer.splitter.RecursiveCharacterTextSplitter;
import host.springboot.framework3.core.enumeration.error.ErrorCodeEnum;
import host.springboot.framework3.core.exception.ApplicationException;
import org.springframework.ai.document.Document;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;

import java.util.List;


/**
 * FileParser
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
public interface FileParser {

    Flux<Document> parse(FilePart files);

    default Flux<Document> transform(Flux<Document> documents) {
        RecursiveCharacterTextSplitter splitter = new RecursiveCharacterTextSplitter();
        return documents.flatMap(filePart -> {
            List<Document> transform = splitter.transform(List.of(filePart));
            return Flux.fromIterable(transform);
        });
    }

    /**
     * 根据文件扩展名获取对应的解析器
     */
    static FileParser create(String extension) {
        return switch (extension) {
            case "md" -> new MarkdownFileParser();
            case "pdf" -> new PdfFileParser();
            case "json" -> new JsonFileParser();
            default ->
                    throw new ApplicationException(ErrorCodeEnum.USER_UPLOAD_FILE_ERROR, "不支持的文件类型: " + extension);
        };
    }
}
