import {defineConfig} from 'vitepress'

// https://vitepress.dev/reference/site-config
export default defineConfig({
    // ----- 站点元数据 -----
    title: "Spring AI RAG Platform",
    description: "Documentation for Spring AI RAG Platform",

    // ----- 构建 -----
    srcDir: "src",
    lang: 'zh-CN',
    metaChunk: true,
    ignoreDeadLinks: [
        /^https?:\/\/localhost/,
    ],

    // ----- 主题 -----
    appearance: 'dark',
    lastUpdated: true,

    themeConfig: {
        i18nRouting: false, // Disabling i18n for now as we only have CN docs initially
        logo: '/logo-mini.png',
        
        // 页脚配置
        footer: {
            message: 'Released under the Apache-2.0 License',
            copyright: 'Copyright © 2026 Spring AI RAG Platform'
        },

        // 导航菜单项的配置。
        nav: [
            {text: '主页', link: '/'},
            {text: '项目概述', link: '/overview/'},
            {text: 'API文档', link: '/api/'}
        ],

        // 侧边栏菜单项的配置。
        sidebar: [
            {
                text: '项目介绍',
                items: [
                    {text: '概述与架构', link: '/overview/'}
                ]
            },
            {
                text: '开发指南',
                items: [
                    {text: '环境配置', link: '/config/env'},
                    {text: '构建与部署', link: '/build/deploy'},
                    {text: 'API 接口', link: '/api/'}
                ]
            },
            {
                text: '常见问题',
                items: [
                    {text: 'FAQ', link: '/faq/'}
                ]
            }
        ],

        docFooter: {
            prev: '上一页',
            next: '下一页'
        }
    }
})
