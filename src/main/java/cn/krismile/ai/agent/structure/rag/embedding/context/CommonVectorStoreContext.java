package cn.krismile.ai.agent.structure.rag.embedding.context;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationConvention;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Common Vector Store Context
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Component
public record CommonVectorStoreContext(
        ObjectProvider<ObservationRegistry> observationRegistry,
        ObjectProvider<VectorStoreObservationConvention> customObservationConvention,
        BatchingStrategy batchingStrategy
) {
}
