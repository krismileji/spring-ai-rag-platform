package cn.krismile.ai.agent.structure.rag.file.parser;

import cn.krismile.ai.agent.configuration.security.context.SecurityUtils;
import cn.krismile.ai.agent.constant.Knowledge;
import cn.krismile.ai.agent.structure.rag.file.FileParser;
import com.alibaba.cloud.ai.transformer.splitter.RecursiveCharacterTextSplitter;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * MarkdownFileParser
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
public class MarkdownFileParser implements FileParser {
    @Override
    public Flux<Document> parse(FilePart file) {
        return SecurityUtils.getUserId()
                .flatMapMany(userId -> file.content()
                .map(dataBuffer -> new InputStreamResource(dataBuffer.asInputStream()))
                .flatMap(resource -> {
                    MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                            // 是否将由水平线（---）分隔的文本创建为新的 Document 对象
                            // .withHorizontalRuleCreateDocument(true)
                            // 所有代码块都在单独的文档中
                            // .withIncludeCodeBlock(true)
                            // 有引用块都在单独的文档中
                            // .withIncludeBlockquote(true)
                            // 添加额外元数据
                            // .withAdditionalMetadata()
                            .withAdditionalMetadata(Knowledge.MetaData.USER_ID, userId)
                            .build();
                    MarkdownDocumentReader reader = new MarkdownDocumentReader(resource, config);
                    List<Document> documents = reader.get();
                    return Flux.fromIterable(documents);
                }));
    }

    @Override
    public Flux<Document> transform(Flux<Document> documents) {
        RecursiveCharacterTextSplitter splitter = new RecursiveCharacterTextSplitter();
        return documents.flatMap(filePart -> {
            List<Document> transform = splitter.transform(List.of(filePart));
            return Flux.fromIterable(transform);
        });
    }
}
