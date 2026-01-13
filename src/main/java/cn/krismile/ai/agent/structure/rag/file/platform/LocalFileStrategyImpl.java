package cn.krismile.ai.agent.structure.rag.file.platform;

import cn.krismile.ai.agent.model.enumeration.FilePlatformEnum;
import cn.krismile.ai.agent.structure.rag.file.FileStorage;
import host.springboot.framework3.core.enumeration.error.ErrorCodeEnum;
import host.springboot.framework3.core.exception.ApplicationException;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * LocalFileStrategyImpl
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
public class LocalFileStrategyImpl implements FileStorage {

    private final Path path;

    public LocalFileStrategyImpl(String path) {
        this.path = Paths.get(path);
        try {
            Files.createDirectories(this.path);
        } catch (IOException e) {
            throw new ApplicationException(ErrorCodeEnum.SYSTEM_READ_DISK_FILE_ERROR, "Failed to create storage directory");
        }
    }

    @Override
    public FilePlatformEnum platform() {
        return FilePlatformEnum.LOCAL;
    }

    @Override
    public Flux<FilePart> uploads(Flux<FilePart> files) {
        return files.subscribeOn(Schedulers.boundedElastic())
                .flatMap(filePart -> {
                    // 生成唯一文件名
                    String originalFilename = filePart.filename();
                    String extension = FilenameUtils.getExtension(originalFilename);
                    String baseName = FilenameUtils.getBaseName(originalFilename);
                    String finalFilename = baseName + "_" + UUID.randomUUID() + "." + extension;
                    Path filePath = this.path.resolve(finalFilename);
                    // 异步写入文件
                    return filePart.transferTo(filePath)
                            .then(Mono.just(filePart))
                            .onErrorMap(e -> new ApplicationException(
                                    ErrorCodeEnum.SYSTEM_READ_DISK_FILE_ERROR,
                                    "Failed to upload file: " + originalFilename
                            ));
                });
    }
}
