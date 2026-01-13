package cn.krismile.ai.agent.structure.rag.file.parser;

import cn.krismile.ai.agent.structure.rag.file.FileParser;
import org.springframework.ai.document.Document;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;

/**
 * PdfFileParser
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
public class PdfFileParser implements FileParser {
    @Override
    public Flux<Document> parse(FilePart files) {
        return Flux.empty();
    }
}
