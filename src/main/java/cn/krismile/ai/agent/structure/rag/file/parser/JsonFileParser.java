package cn.krismile.ai.agent.structure.rag.file.parser;

import cn.krismile.ai.agent.structure.rag.file.FileParser;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.JsonReader;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * JsonFileParser
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
public class JsonFileParser implements FileParser {
    @Override
    public Flux<Document> parse(FilePart file) {
        return file.content()
                .map(dataBuffer -> new InputStreamResource(dataBuffer.asInputStream()))
                .flatMap(resource -> {
                    JsonReader reader = new JsonReader(resource);
                    List<Document> documents = reader.get();
                    return Flux.fromIterable(documents);
                });
    }
}
