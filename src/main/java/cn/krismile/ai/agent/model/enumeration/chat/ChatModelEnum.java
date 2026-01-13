package cn.krismile.ai.agent.model.enumeration.chat;

import host.springboot.framework3.core.enumeration.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 聊天模型枚举
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum ChatModelEnum implements BaseEnum<String> {

    /**
     * 通义千问3:4b
     */
    OLLAMA_QWEN3_4B("qwen3:4b", "通义千问3:4b", ""),

    /**
     * DeepSeek-V3.2（非思考模式）
     */
    DEEPSEEK_CHAT("deepseek-chat", "DeepSeek-V3.2（非思考模式）", ""),

    /**
     * DeepSeek-V3.2（思考模式）
     */
    DEEPSEEK_REASONER("deepseek-reasoner", "DeepSeek-V3.2（思考模式）", ""),

    /**
     * 通义千问3-Max
     */
    QWEN3_MAX("qwen3-max", "通义千问3-Max",
            "通义千问3系列Max模型，相较preview版本在智能体编程与工具调用方向进行了专项升级。本次发布的正式版模型达到领域SOTA水平，适配场景更加复杂的智能体需求。"),

    /**
     * 通义千问3开源模型
     */
    QWEN3_NEXT_80B_A3B_INSTRUCT("qwen3-next-80b-a3b-instruct", "通义千问3开源模型",
            "Qwen3系列开源模型，包含混合模型、思考模型与非思考模型，思考能力与通用能力均达到同规模业界SOTA水平。"),

    /**
     * 通义千问-Plus
     */
    QWEN_PLUS("qwen-plus", "通义千问-Plus",
            "通义千问超大规模语言模型的增强版，支持中文英文等不同语言输入。主干模型、latest和快照04-28已升级Qwen3系列，实现思考模式和非思考模式的有效融合，可在对话中切换模式。"),

    /**
     * 通义千问-Flash
     */
    QWEN_FLASH("qwen-flash", "通义千问-Flash",
            "Qwen3系列Flash模型，实现思考模式和非思考模式的有效融合，可在对话中切换模式。复杂推理类任务性能优秀，指令遵循、文本理解等能力显著提高。支持1M上下文长度，按照上下文长度进行阶梯计费。"),

    /**
     * 通义千问-Max
     */
    QWEN_MAX("qwen-max", "通义千问-Max",
            "通义千问2.5系列千亿级别超大规模语言模型，支持中文、英文等不同语言输入。随着模型的升级，qwen-max将滚动更新升级。如果希望使用固定版本，请使用历史快照版本。"),

    /**
     * 通义千问3-Coder-Plus
     */
    QWEN3_CODER_PLUS("qwen3-coder-plus", "通义千问3-Coder-Plus",
            "基于Qwen3的代码生成模型，具有强大的Coding Agent能力，擅长工具调用和环境交互，能够实现自主编程、代码能力卓越的同时兼具通用能力。"),

    /**
     * 通义千问3-Coder-Flash
     */
    QWEN3_CODER_FLASH("qwen3-coder-flash", "通义千问3-Coder-Flash",
            "基于Qwen3的代码生成模型，继承Qwen3-Coder-Plus的coding agent能力，支持多轮工具交互，重点优化仓库级别理解能力并增加工具调用稳定性。"),

    /**
     * 通义千问深入研究
     */
    QWEN_DEEP_RESEARCH("qwen-deep-research", "通义千问深入研究",
            "通义千问深入研究是一款面向复杂研究任务的高级智能体系统，具备多轮推理与全局规划能力，能够运用互联网搜索等多种工具，对任务进行精细化拆解，开展推理与分析，最终为用户生成可溯源、逻辑严谨的研究型报告。"),

    /**
     * 通义千问-MT-Plus
     */
    QWEN_MT_PLUS("qwen-mt-plus", "通义千问-MT-Plus",
            "基于Qwen3全面升级的旗舰级翻译大模型，支持92个语种互译，模型性能和翻译效果全面升级，并提供更稳定的术语定制、格式还原度、领域提示能力，让译文更精准、自然。"),

    /**
     * 通义千问-MT-Flash
     */
    QWEN_MT_FLASH("qwen-mt-flash", "通义千问-MT-Flash",
            "基于Qwen3全面升级的轻量级文本翻译大模型，支持92个语种互译，模型性能和翻译效果全面升级，并提供更稳定的术语定制、格式还原度、领域提示能力，让译文更精准、自然。"),

    /**
     * 通义千问-MT-Turbo
     */
    QWEN_MT_TURBO("qwen-mt-turbo", "通义千问-MT-Turbo",
            "基于Qwen3全面升级的轻量级文本翻译大模型，支持92个语种互译，模型性能和翻译效果全面升级，提供更稳定的术语定制、格式还原度、领域提示能力，让译文更精准、自然。"),

    /**
     * 通义千问3-Coder-480B-A35B-Instruct
     */
    QWEN3_CODER_480B_A35B_INSTRUCT("qwen3-coder-480b-a35b-instruct",
            "通义千问3-Coder-480B-A35B-Instruct", "基于Qwen3的代码生成模型，具有强大的Coding Agent能力，代码能力达到开源模型 SOTA。"),

    /**
     * 通义千问3-Coder-30B-A3B-Instruct
     */
    QWEN3_CODER_30B_A3B_INSTRUCT("qwen3-coder-30b-a3b-instruct",
            "通义千问3-Coder-30B-A3B-Instruct", "基于Qwen3的代码生成模型，继承Qwen3-Coder-480B-A35B-Instruct的coding agent能力，代码能力达到同尺寸规模模型SOTA。"),

    /**
     * DeepSeek
     */
    DEEPSEEK_V3_2("deepseek-v3.2", "DeepSeek",
            "DeepSeek是由深度求索提供的开源模型，包含 V3.1、V3、R1以及基于Qwen2.5系列蒸馏的大语言模型。"),

    /**
     * GLM
     */
    GLM_4_7("glm-4.7", "GLM",
            "GLM是由智谱提供的开源模型。"),

    /**
     * Kimi-K2
     */
    KIMI_K2_THINKING("kimi-k2-thinking", "Kimi-K2",
            "Kimi-K2是由月之暗面提供的开源模型，包含Moonshot-Kimi-K2-Instruct、kimi-k2-thinking系列模型，具有卓越的编码和工具调用能力。"),

    /**
     * 通义千问2.5开源模型
     */
    QWEN2_5_7B_INSTRUCT_1M("qwen2.5-7b-instruct-1m", "通义千问2.5开源模型",
            "Qwen2.5系列开源模型，包含文本生成模型、视觉理解模型、多模态模型等多个领域领先模型。"),

    /**
     * 通义千问-Turbo
     */
    QWEN_TURBO("qwen-turbo", "通义千问-Turbo",
            "通义千问超大规模语言模型，支持中文英文等不同语言输入。主干模型、latest和快照04-28已升级Qwen3系列，实现思考模式和非思考模式的有效融合，可在对话中切换模式。"),

    /**
     * 通义千问-MT-Lite
     */
    QWEN_MT_LITE("qwen-mt-lite", "通义千问-MT-Lite",
            "基于Qwen3全面升级的基础级文本翻译大模型，支持32个语种互译，模型性能和翻译效果全面升级，并提供更稳定的术语定制、格式还原度、领域提示能力，让译文更精准、自然。"),

    /**
     * 通义千问-QwQ-32B-Preview
     */
    QWQ_32B_PREVIEW("qwq-32b-preview", "通义千问-QwQ-32B-Preview",
            "QwQ模型是由 Qwen 团队开发的实验性研究模型，专注于增强 AI 推理能力。"),

    /**
     * 通义千问2开源模型
     */
    QWEN2_0_5B_INSTRUCT("qwen2-0.5b-instruct", "通义千问2开源模型",
            "Qwen2系列开源模型，包含文本生成模型、视觉理解模型等多领域领先模型。"),

    /**
     * 通义千问-Math-Plus
     */
    QWEN_MATH_PLUS("qwen-math-plus", "通义千问-Math-Plus",
            "通义千问数学模型具有强大的数学解题能力,擅长处理中英文数学题，包括方程、计算、证明等方向。"),

    /**
     * 通义千问-Math-Turbo
     */
    QWEN_MATH_TURBO("qwen-math-turbo", "通义千问-Math-Turbo",
            "通义千问系列数学模型是专门用于数学解题的语言模型，推理速度快，成本低。"),

    /**
     * 通义千问-Coder-Turbo
     */
    QWEN_CODER_TURBO("qwen-coder-turbo", "通义千问-Coder-Turbo",
            "通义千问系列代码及编程模型是专门用于编程和代码生成的语言模型，推理速度快，成本低。"),

    /**
     * 意图分类模型
     */
    TONGYI_INTENT_DETECT_V3("tongyi-intent-detect-v3", "意图分类模型",
            "意图识别和槽位填充是对话系统中的基础任务。本模型实现了一个基于 API的意图（intent）和槽位参数（slots）联合预测。在一次模型输出中，同时完成多个指令API的返回和槽位参数的填充。返回的结果为标准json格式。"),

    /**
     * Qwen-Long
     */
    QWEN_LONG("qwen-long", "Qwen-Long",
            "Qwen-Long是在通义千问针对超长上下文处理场景的大语言模型，支持中文、英文等不同语言输入，支持最长1000万tokens(约1500万字或1.5万页文档)的超长上下文对话。配合同步上线的文档服务，可支持文本文件（ TXT、DOCX、PDF、XLSX、EPUB、MOBI、MD、CSV）和图片文件（BMP、PNG、JPG/JPEG、GIF 以及PDF扫描件）的解析和对话。说明：通过HTTP直接提交请求，支持1M tokens长度，超过此长度建议通过文件方式提交。"),

    /**
     * 通义千问1.5开源模型
     */
    QWEN1_5_110B_CHAT("qwen1.5-110b-chat", "通义千问1.5开源模型",
            "Qwen1.5系列开源模型。"),

    /**
     * 通义千问-doc-turbo
     */
    QWEN_DOC_TURBO("qwen-doc-turbo", "通义千问-doc-turbo",
            "快速对文档进行精准信息抽取，打标分类，内容审核及摘要总结。"),

    /**
     * 通义千问-Plus-Character
     */
    QWEN_PLUS_CHARACTER("qwen-plus-character", "通义千问-Plus-Character",
            "通义千问系列角色扮演模型，本模型是动态更新版本，模型更新会提前通知，适合拟人化的角色扮演，同时优化了限定人设指令遵循、话题推进、倾听共情等能力，支持个性化角色的深度还原。"),

    /**
     * 通义千问-Coder-Plus
     */
    QWEN_CODER_PLUS("qwen-coder-plus", "通义千问-Coder-Plus",
            "通义千问系列代码及编程模型是专门用于编程和代码生成的语言模型，性能出色，效果突出。"),

    /**
     * MiniMax abab6.5g-8k
     */
    ABAB6_5G_CHAT("abab6.5g-chat", "MiniMax abab6.5g-8k",
            "abab6.5g是MiniMax推出的大语言模型，适用于英文人设对话场景，在英文语言支持、人设保持、指令遵从、生成趣味上会有比较好的效果。"),

    /**
     * MiniMax abab6.5t-8k
     */
    ABAB6_5T_CHAT("abab6.5t-chat", "MiniMax abab6.5t-8k",
            "abab6.5t是MiniMax推出的大语言模型，适用于中文人设对话场景，在人设保持、指令遵从、意图理解、生成趣味上会有比较好的效果。"),

    /**
     * MiniMax abab6.5s-245k
     */
    ABAB6_5S_CHAT("abab6.5s-chat", "MiniMax abab6.5s-245k",
            "abab6.5s 是MiniMax推出的万亿参数大语言模型，性价比极高，适用于通用场景，特别是在生产力复杂任务场景上表现较好，最大支持245k上下文窗口，支持搜索、function call等功能。"),

    /**
     * 通义法睿-Plus-32K
     */
    FARUI_PLUS("farui-plus", "通义法睿-Plus-32K",
            "通义法睿是以通义千问为基座经法律行业数据和知识专门训练的法律行业大模型产品，综合运用了模型精调、强化学习、 RAG检索增强、法律Agent技术，具有回答法律问题、推理法律适用、推荐裁判类案、辅助案情分析、生成法律文书、检索法律知识、审查合同条款等功能。"),

    /**
     * OpenNLU开放域文本理解模型
     */
    OPENNLU_V1("opennlu-v1", "OpenNLU开放域文本理解模型",
            "OpenNLU是开箱即用的文本理解大模型，适用于零样本、少样本条件下进行文本理解任务，如信息抽取、文本分类等。");

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举描述
     */
    private final String reasonPhrase;

    /**
     * 描述
     */
    private final String description;

    public ChatPlatformEnum platform() {
        return switch (this) {
            case OLLAMA_QWEN3_4B -> ChatPlatformEnum.OLLAMA;
            case DEEPSEEK_CHAT, DEEPSEEK_REASONER -> ChatPlatformEnum.DEEPSEEK;
            default -> ChatPlatformEnum.ALIYUN;
        };
    }
}