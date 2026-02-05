package cn.krismile.ai.agent.structure.chat.tool;

import cn.krismile.ai.agent.exception.tool.WebVisitException;
import cn.krismile.ai.agent.model.enumeration.tool.WebVisitErrorEnum;
import host.springboot.framework3.core.enumeration.BaseEnum;
import host.springboot.framework3.core.logging.LoggingComponent;
import host.springboot.framework3.core.model.Pair;
import host.springboot.framework3.core.response.R;
import host.springboot.framework3.core.response.vo.VO;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 网页访问工具
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Component
public class WebVisitTool implements LoggingComponent {

    private static final String METRIC_PREFIX = "ai.tool.web_visit";
    private static final Duration TIMEOUT = Duration.ofSeconds(20);
    private static final int KEEP_FIRST_PARAGRAPHS = 5;
    private static final int KEEP_LONGEST_PARAGRAPHS = 10;

    private final RateLimiter rateLimiter = RateLimiter.of("webVisitRateLimiter", RateLimiterConfig.custom()
            .timeoutDuration(Duration.ofSeconds(30))
            .limitRefreshPeriod(Duration.ofMillis(500))
            .limitForPeriod(50)
            .build());
    private final Retry retry = Retry.backoff(3, Duration.ofMillis(200))
            .maxBackoff(Duration.ofSeconds(2))
            .filter(this::isRetryable);

    @Resource
    private WebClient.Builder webClientBuilder;
    @Resource
    private ObjectProvider<MeterRegistry> meterRegistryProvider;

    /**
     * 访问网页并返回统一响应
     *
     * @param url 访问 URL
     * @return 统一响应结果
     * @since 1.0.0
     */
    @SuppressWarnings("unused")
    @Tool(name = "visit_web", description = "visit a website")
    public VO<?> visit(@ToolParam(description = "the url of the website") String url) {
        long startTime = System.nanoTime();
        return Mono.defer(() -> {
            if (!this.isUrlValid(url)) {
                throw new WebVisitException(WebVisitErrorEnum.INVALID_URL,
                        WebVisitErrorEnum.INVALID_URL.getReasonPhrase(), null, false);
            }
            if (!this.rateLimiter.acquirePermission()) {
                throw new WebVisitException(WebVisitErrorEnum.RATE_LIMIT,
                        WebVisitErrorEnum.RATE_LIMIT.getReasonPhrase(), null, true);
            }
            return this.fetchHtml(url)
                    .flatMap(html -> Mono.fromCallable(() -> this.parseHtml(html, url))
                            .subscribeOn(Schedulers.boundedElastic())
                            .map(this::buildFormattedContent)
                            .doOnNext(processed -> this.recordSuccessMetrics(url, processed, startTime))
                            .<VO<?>>map(R::data));
        }).onErrorResume(ex -> Mono.just(this.handleError(ex, url, startTime))).block();
    }

    /**
     * 拉取 HTML 内容
     *
     * @param url 访问 URL
     * @return HTML 文本
     * @since 1.0.0
     */
    private Mono<String> fetchHtml(String url) {
        return this.webClientBuilder.build().get()
                .uri(url)
                .headers(this::applyHeaders)
                .exchangeToMono(this::handleResponse)
                .timeout(TIMEOUT)
                .retryWhen(this.retry);
    }

    /**
     * 处理 HTTP 响应
     *
     * @param response 响应对象
     * @return HTML 文本
     * @since 1.0.0
     */
    private Mono<String> handleResponse(ClientResponse response) {
        if (response.statusCode().is2xxSuccessful()) {
            return response.bodyToMono(String.class);
        }
        HttpStatusCode statusCode = response.statusCode();
        return response.bodyToMono(String.class)
                .defaultIfEmpty("")
                .flatMap(body -> Mono.error(new WebVisitException(
                        WebVisitErrorEnum.HTTP_ERROR, body,
                        statusCode.value(),
                        statusCode.is5xxServerError())));
    }

    /**
     * 设置请求头
     *
     * @param headers 请求头
     * @since 1.0.0
     */
    private void applyHeaders(HttpHeaders headers) {
        headers.set(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml,application/xml");
        headers.set(HttpHeaders.ACCEPT_LANGUAGE, "zh-CN,zh;q=0.9,en;q=0.8");
    }

    /**
     * 解析 HTML 内容
     *
     * @param html HTML 内容
     * @param url  访问 URL
     * @return 解析结果
     * @since 1.0.0
     */
    private ParsedContent parseHtml(String html, String url) {
        try {
            Document document = Jsoup.parse(html, url);
            document.outputSettings().charset(StandardCharsets.UTF_8);
            document.select("script,style,noscript,svg,iframe,canvas,form,nav,footer,header,aside").remove();
            String title = this.normalizeText(document.title());
            String description = this.extractMetaDescription(document);
            Element mainElement = this.extractMainElement(document);
            List<String> paragraphs = this.extractParagraphs(mainElement);
            String fallbackText = this.normalizeText(mainElement.text());
            return new ParsedContent(title, description, paragraphs, fallbackText);
        } catch (Exception e) {
            throw new WebVisitException(WebVisitErrorEnum.PARSE_ERROR, e.getLocalizedMessage(), null, false);
        }
    }

    /**
     * 提取元信息描述
     *
     * @param document 文档对象
     * @return 描述文本
     * @since 1.0.0
     */
    private String extractMetaDescription(Document document) {
        return Stream.of("meta[name=description]", "meta[property=og:description]")
                .map(document::selectFirst)
                .filter(Objects::nonNull)
                .map(el -> el.attr("content"))
                .filter(StringUtils::isNotBlank)
                .findFirst()
                .map(this::normalizeText)
                .orElse("");
    }

    /**
     * 提取主体元素
     *
     * @param document 文档对象
     * @return 主体元素
     * @since 1.0.0
     */
    private Element extractMainElement(Document document) {
        return Optional.ofNullable(document.selectFirst("article,main,[role=main]")).orElseGet(document::body);
    }

    /**
     * 提取段落文本
     *
     * @param mainElement 主体元素
     * @return 段落列表
     * @since 1.0.0
     */
    private List<String> extractParagraphs(Element mainElement) {
        return mainElement.select("p").stream()
                .map(element -> this.normalizeText(element.text()))
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
    }

    /**
     * 生成格式化内容
     *
     * @param parsed 解析结果
     * @return 处理结果
     * @since 1.0.0
     */
    private String buildFormattedContent(ParsedContent parsed) {
        String titleLine = StringUtils.isNotBlank(parsed.title()) ? "title: " + parsed.title() : "";
        String descriptionLine = StringUtils.isNotBlank(parsed.description()) ? "description: " + parsed.description() : "";
        String prefix = String.join("\n", Stream.of(titleLine, descriptionLine)
                .filter(StringUtils::isNotBlank)
                .toList());
        String prefixWithSeparator = StringUtils.isNotBlank(prefix) ? prefix + "\n" : "";

        String body = this.buildBody(parsed);
        return prefixWithSeparator + (StringUtils.isNotBlank(body) ? "body: " + body : "");
    }

    /**
     * 构建正文内容
     *
     * @param parsed 解析结果
     * @return 正文文本
     * @since 1.0.0
     */
    private String buildBody(ParsedContent parsed) {
        if (!CollectionUtils.isEmpty(parsed.paragraphs())) {
            List<String> selected = this.selectKeyParagraphs(parsed.paragraphs());
            return String.join("\n", selected).trim();
        }
        return parsed.fallbackText();
    }

    /**
     * 选择重点段落
     *
     * @param paragraphs 段落列表
     * @return 重点段落
     * @since 1.0.0
     */
    private List<String> selectKeyParagraphs(List<String> paragraphs) {
        int keepFirst = Math.min(KEEP_FIRST_PARAGRAPHS, paragraphs.size());
        int keepLongest = Math.min(KEEP_LONGEST_PARAGRAPHS, paragraphs.size());
        List<String> longest = new ArrayList<>(paragraphs);
        longest.sort((a, b) -> Integer.compare(b.length(), a.length()));
        Set<String> selected = new LinkedHashSet<>(paragraphs.subList(0, keepFirst));
        selected.addAll(longest.subList(0, keepLongest));
        List<String> ordered = new ArrayList<>();
        for (String paragraph : paragraphs) {
            if (selected.contains(paragraph)) {
                ordered.add(paragraph);
            }
        }
        return ordered;
    }

    /**
     * 规范化文本
     *
     * @param text 原始文本
     * @return 规范化文本
     * @since 1.0.0
     */
    private String normalizeText(String text) {
        if (StringUtils.isBlank(text)) {
            return "";
        }
        // 将“不换行空格”（Non-breaking space, &nbsp; ）替换为普通空格
        return text.replace('\u00A0', ' ')
                // 将制表符 ( \t )、垂直制表符 ( \x0B )、换页符 ( \f )、回车符 ( \r ) 等控制字符替换为单个空格
                .replaceAll("[\\t\\x0B\\f\\r]+", " ")
                // 将连续的多个空格合并为一个空格。
                .replaceAll(" +", " ")
                // 清理换行符前后的空白字符（包括空格、制表符等）
                .replaceAll("\\s*\\n\\s*", "\n")
                // 将连续 3 个及以上的换行符（即连续 2 个及以上的空行）替换为 2 个换行符（即 1 个空行）
                .replaceAll("\\n{3,}", "\n\n")
                // 去除整个字符串首尾的空白字符
                .trim();
    }

    /**
     * 判断是否可重试
     *
     * @param throwable 异常对象
     * @return 是否可重试
     * @since 1.0.0
     */
    private boolean isRetryable(Throwable throwable) {
        if (throwable instanceof TimeoutException) {
            return true;
        }
        if (throwable instanceof WebVisitException visitException) {
            return visitException.isRetryable();
        }
        return false;
    }

    /**
     * 处理异常并生成标准响应
     *
     * @param ex        异常信息
     * @param url       访问 URL
     * @param startTime 开始时间
     * @return 错误响应
     * @since 1.0.0
     */
    private VO<?> handleError(Throwable ex, String url, long startTime) {
        Throwable root = ex;
        while (root != null && root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        if (root instanceof WebVisitException visitException) {
            return this.buildErrorResponse(
                    url,
                    visitException.getStatus(),
                    visitException.getErrorEnum(),
                    visitException.getMessage(),
                    startTime);
        }
        if (root instanceof TimeoutException) {
            return this.buildErrorResponse(url, null, WebVisitErrorEnum.TIMEOUT, null, startTime);
        }
        return this.buildErrorResponse(url, null,
                WebVisitErrorEnum.FETCH_ERROR,
                root != null ? root.getLocalizedMessage() : null, startTime);
    }

    /**
     * 生成错误响应
     *
     * @param url       访问 URL
     * @param status    状态码
     * @param errorEnum 错误枚举
     * @param message   错误消息
     * @param startTime 开始时间
     * @return 错误响应
     * @since 1.0.0
     */
    private VO<?> buildErrorResponse(
            @NonNull String url, @Nullable Integer status,
            @NonNull BaseEnum<String> errorEnum,
            @Nullable String message, long startTime) {
        long durationMs = Duration.ofNanos(System.nanoTime() - startTime).toMillis();
        logInstance().error("网页访问失败", List.of(
                Pair.of("url", url),
                Pair.of("durationMs", durationMs),
                Pair.of("status", status),
                Pair.of("errorEnum", errorEnum),
                Pair.of("message", message)
        ));
        this.recordFailureMetrics(url, errorEnum.getValue(), status, startTime);
        // noinspection DataFlowIssue
        return R.fail(errorEnum.getValue(), StringUtils.defaultIfBlank(message, errorEnum.getReasonPhrase()), errorEnum.getReasonPhrase());
    }

    /**
     * 记录成功指标与日志
     *
     * @param url       访问 URL
     * @param processed 处理结果
     * @param startTime 开始时间
     * @since 1.0.0
     */
    private void recordSuccessMetrics(String url, String processed, long startTime) {
        long durationMs = Duration.ofNanos(System.nanoTime() - startTime).toMillis();
        logInstance().info("网页访问成功", List.of(
                Pair.of("url", url),
                Pair.of("responseLength", processed.length()),
                Pair.of("durationMs", durationMs)));
        MeterRegistry registry = this.meterRegistryProvider.getIfAvailable();
        if (registry != null) {
            Timer.builder(METRIC_PREFIX + ".duration")
                    .tag("status", "success")
                    .tag("host", this.extractHost(url))
                    .register(registry)
                    .record(Duration.ofNanos(System.nanoTime() - startTime));
            DistributionSummary.builder(METRIC_PREFIX + ".response_length")
                    .register(registry)
                    .record(processed.length());
            Counter.builder(METRIC_PREFIX + ".calls")
                    .tag("status", "success")
                    .tag("host", this.extractHost(url))
                    .register(registry)
                    .increment();
        }
    }

    /**
     * 记录失败指标与日志
     *
     * @param url        访问 URL
     * @param errorCode  错误码
     * @param httpStatus Http 错误码
     * @param startTime  开始时间
     * @since 1.0.0
     */
    private void recordFailureMetrics(String url, String errorCode, Integer httpStatus, long startTime) {
        Optional.ofNullable(this.meterRegistryProvider.getIfAvailable()).ifPresent(registry -> {
            String host = this.extractHost(url);
            String httpStatusString = String.valueOf(httpStatus);
            Timer.builder(METRIC_PREFIX + ".duration")
                    .tag("status", "error")
                    .tag("host", host)
                    .tag("httpStatus", httpStatusString)
                    .tag("errorCode", errorCode)
                    .register(registry)
                    .record(Duration.ofNanos(System.nanoTime() - startTime));
            Counter.builder(METRIC_PREFIX + ".calls")
                    .tag("status", "error")
                    .tag("host", host)
                    .tag("httpStatus", httpStatusString)
                    .tag("errorCode", errorCode)
                    .register(registry)
                    .increment();
        });
    }

    /**
     * 提取主机名
     *
     * @param url 访问 URL
     * @return 主机名
     * @since 1.0.0
     */
    private String extractHost(String url) {
        try {
            URI uri = new URI(url);
            return StringUtils.defaultIfBlank(uri.getHost(), "unknown");
        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * 校验 URL 合法性
     *
     * @param url 访问 URL
     * @return 是否合法
     * @since 1.0.0
     */
    private boolean isUrlValid(String url) {
        if (StringUtils.isBlank(url)) {
            return false;
        }
        try {
            URI uri = new URI(url);
            if (StringUtils.isBlank(uri.getScheme())) {
                return false;
            }
            return "http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme());
        } catch (URISyntaxException e) {
            return false;
        }
    }

    @Override
    public @NonNull String logTag() {
        return "网页访问工具";
    }

    /**
     * 解析结果
     *
     * @param title        标题
     * @param description  描述
     * @param paragraphs   段落
     * @param fallbackText 回退文本
     * @since 1.0.0
     */
    private record ParsedContent(String title, String description, List<String> paragraphs, String fallbackText) {
    }
}
