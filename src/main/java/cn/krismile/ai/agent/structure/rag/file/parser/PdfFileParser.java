package cn.krismile.ai.agent.structure.rag.file.parser;

import cn.krismile.ai.agent.structure.rag.file.FileParser;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.ParagraphPdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * PdfFileParser
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
public class PdfFileParser implements FileParser {
    @Override
    public Flux<Document> parse(FilePart file) {
        return file.content()
                .map(dataBuffer -> new InputStreamResource(dataBuffer.asInputStream()))
                .flatMap(resource -> {
                    PdfDocumentReaderConfig config = PdfDocumentReaderConfig.builder()
                            // .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                            // 将文档文本向左对齐。默认为 false
                            // .withLeftAlignment(false)
                            // 从文本上下行删除前跳过前 N 页。默认为 0。
                            // .withNumberOfTopPagesToSkipBeforeDelete(0)
                            // 从页面文本中删除前 N 行。默认为 0
                            // .withNumberOfTopTextLinesToDelete(0)
                            // 从页面文本中删除底部 N 行。默认为 0
                            // .withNumberOfBottomTextLinesToDelete(0)
                            // 设置格式化文本时使用的行分隔符。默认为系统行分隔符
                            // .overrideLineSeparator(System.lineSeparator())
                            // .build())
                            // 一个 Document 实例中要放多少页。0 代表所有页面。默认为 1
                            // .withPagesPerDocument(1)
                            // 配置 PDF 页面上边距。默认为 0
                            // .withPageTopMargin(0)
                            // 配置 PDF 页面下边距。默认为 0
                            // .withPageBottomMargin(0)
                            // 配置 PDF 反向段落位置。默认为 false。
                            // .withReversedParagraphPosition(false)
                            .build();
                    // 页面解析
                    // PagePdfDocumentReader reader = new PagePdfDocumentReader(resource, config);
                    // 段落解析
                    ParagraphPdfDocumentReader reader = new ParagraphPdfDocumentReader(resource, config);
                    List<Document> documents = reader.get();
                    return Flux.fromIterable(documents);
                });
    }
}
