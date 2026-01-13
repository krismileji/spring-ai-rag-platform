package cn.krismile.ai.agent.structure.rag.file;

import cn.krismile.ai.agent.model.enumeration.FilePlatformEnum;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;

/**
 * FilePlatformStrategy
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
public interface FileStorage {

    /**
     * 获取平台枚举
     *
     * @return 平台枚举
     * @since 1.0.0
     */
    FilePlatformEnum platform();

    /**
     * 上传文件
     *
     * @param files 文件
     * @return 是否成功
     * @since 1.0.0
     */
    Flux<FilePart> uploads(Flux<FilePart> files);

}