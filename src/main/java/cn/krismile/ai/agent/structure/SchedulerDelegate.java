package cn.krismile.ai.agent.structure;

import cn.krismile.ai.agent.context.RequestContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.concurrent.TimeUnit;

/**
 * SchedulerDelegate
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class SchedulerDelegate implements Scheduler {

    private final Scheduler scheduler;
    private final ServerWebExchange exchange;

    public static SchedulerDelegate create(Scheduler scheduler) {
        return new SchedulerDelegate(scheduler, RequestContext.getExchange());
    }

    @Override
    public @NonNull Disposable schedule(@NonNull Runnable task) {
        return this.scheduler.schedule(() -> RequestContext.asyncApply(this.exchange, task));
    }

    @Override
    public void dispose() {
        this.scheduler.dispose();
    }

    @Override
    public @NonNull Mono<Void> disposeGracefully() {
        return this.scheduler.disposeGracefully();
    }

    @Override
    public void init() {
        this.scheduler.init();
    }

    @Override
    public @NonNull Disposable schedule(@NonNull Runnable task, long delay, @NonNull TimeUnit unit) {
        return this.scheduler.schedule(() -> RequestContext.asyncApply(this.exchange, task), delay, unit);
    }

    @Override
    public @NonNull Disposable schedulePeriodically(
            @NonNull Runnable task,
            long initialDelay, long period,
            @NonNull TimeUnit unit) {
        return this.scheduler.schedulePeriodically(
                () -> RequestContext.asyncApply(this.exchange, task),
                initialDelay, period, unit);
    }

    @Override
    public boolean isDisposed() {
        return this.scheduler.isDisposed();
    }

    @Override
    public long now(@NonNull TimeUnit unit) {
        return this.scheduler.now(unit);
    }

    @Override
    public @NonNull Worker createWorker() {
        return WorkerDelegate.create(this.scheduler.createWorker(), this.exchange);
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class WorkerDelegate implements Worker {

        private final Worker worker;
        private final ServerWebExchange exchange;

        public static WorkerDelegate create(Worker worker, ServerWebExchange exchange) {
            return new WorkerDelegate(worker, exchange);
        }

        @Override
        public @NonNull Disposable schedule(@NonNull Runnable task) {
            return this.worker.schedule(() -> RequestContext.asyncApply(this.exchange, task));
        }

        @Override
        public @NonNull Disposable schedule(@NonNull Runnable task, long delay, @NonNull TimeUnit unit) {
            return this.worker.schedule(() -> RequestContext.asyncApply(this.exchange, task), delay, unit);
        }

        @Override
        public @NonNull Disposable schedulePeriodically(@NonNull Runnable task, long initialDelay, long period, @NonNull TimeUnit unit) {
            return this.worker.schedulePeriodically(
                    () -> RequestContext.asyncApply(this.exchange, task),
                    initialDelay, period, unit);
        }

        @Override
        public void dispose() {
            this.worker.dispose();
        }

        @Override
        public boolean isDisposed() {
            return this.worker.isDisposed();
        }
    }
}
