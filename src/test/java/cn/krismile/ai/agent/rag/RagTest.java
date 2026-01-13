package cn.krismile.ai.agent.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * RagTest
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@SpringBootTest
public class RagTest {

    @Resource
    private VectorStore vectorStore;

    @Test
    void searchDocuments() {
        List<Document> documents = vectorStore.similaritySearch(SearchRequest.builder()
                .query("如何快速开始")
                .build());
        for (Document document : documents) {
            System.out.println(document);
        }
    }
}
