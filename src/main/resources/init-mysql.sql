create table ai_platform
(
    id              bigint            not null comment 'ID' primary key,
    platform        varchar(32)       not null comment '平台',
    api_key         varchar(128)      null comment 'ApiKey',
    default_options json              null comment '默认配置',
    enabled         tinyint default 1 not null comment '是否启用',
    create_time     datetime          not null comment '创建时间',
    create_user     varchar(64)       null comment '创建人',
    update_time     datetime          not null comment '更新时间',
    update_user     varchar(64)       null comment '更新人',
    constraint uk_platform unique (platform),
    index idx_enabled (enabled)
) comment 'AI平台';

create table ai_model
(
    id              bigint            not null comment 'ID' primary key,
    type            varchar(32)       not null comment '模型类型',
    code            varchar(32)       not null comment '模型编码',
    name            varchar(64)       not null comment '模型名称',
    description     varchar(255)      null comment '模型描述',
    default_options json              null comment '默认配置',
    meta_data json null comment '元数据',
    enabled         tinyint default 1 not null comment '是否启用',
    sort            int               not null comment '排序',
    rel_platform_id bigint            not null comment '关联平台ID',
    create_time     datetime          not null comment '创建时间',
    create_user     varchar(64)       null comment '创建人',
    update_time     datetime          not null comment '更新时间',
    update_user     varchar(64)       null comment '更新人',
    constraint uk_code unique (code),
    index idx_enabled_sort (enabled, sort),
    index idx_rel_platform_id (rel_platform_id)
) comment 'AI模型';

create table sys_dict
(
    id            bigint        not null comment 'ID' primary key,
    type          varchar(64)   not null comment '字典类型',
    name          varchar(64)   not null comment '字典名称',
    value         text          not null comment '字典值',
    description   varchar(512)  null comment '字典描述',
    level         int default 1 not null comment '字典层级',
    rel_parent_id bigint        null comment '关联上级ID',
    create_time   datetime      not null comment '创建时间',
    create_user   varchar(64)   null comment '创建人',
    update_time   datetime      not null comment '更新时间',
    update_user   varchar(64)   null comment '更新人'
) comment '系统字典';

create table sys_dict_type
(
    id          bigint       not null comment 'ID' primary key,
    type        varchar(64)  not null comment '类型',
    name        varchar(64)  not null comment '名称',
    description varchar(512) null comment '描述',
    create_time datetime     not null comment '创建时间',
    create_user varchar(64)  null comment '创建人',
    update_time datetime     not null comment '更新时间',
    update_user varchar(64)  null comment '更新人'
) comment '系统字典类型';

create table user
(
    id          bigint       not null comment 'ID' primary key,
    username    varchar(64)  not null comment '用户名',
    password    varchar(128) not null comment '密码',
    nickname    varchar(64)  null comment '昵称',
    avatar      varchar(255) null comment '头像',
    email       varchar(64)  null comment '邮箱',
    mobile      varchar(32)  null comment '手机号',
    status      varchar(32)  not null comment '状态',
    create_time datetime     not null comment '创建时间',
    create_user varchar(64)  null comment '创建人',
    update_time datetime     not null comment '更新时间',
    update_user varchar(64)  null comment '更新人',
    constraint uk_email unique (email),
    constraint uk_mobile unique (mobile),
    constraint uk_username unique (username)
) comment '用户表';

create table user_chat_conversation
(
    id          varchar(64) not null comment 'ID' primary key,
    content     text        not null comment '会话内容',
    rel_user_id bigint      null comment '关联用户ID',
    create_time datetime    not null comment '创建时间',
    create_user varchar(64) null comment '创建人',
    update_time datetime    not null comment '更新时间',
    update_user varchar(64) null comment '更新人',
    index idx_rel_user_id_create_time (rel_user_id asc, create_time desc),
    index idx_rel_user_id (rel_user_id)
) comment '用户会话';

create table user_chat_memory
(
    id                  bigint      not null comment 'ID' primary key,
    model               varchar(32) null comment '模型',
    content             text        not null comment '会话内容',
    reasoning_content   text        null comment '深度思考内容',
    type                varchar(32) not null comment '会话类型',
    rel_conversation_id varchar(64) not null comment '关联会话ID',
    rel_user_id         bigint      null comment '关联用户ID',
    create_time         datetime    not null comment '创建时间',
    create_user         varchar(64) null comment '创建人',
    update_time         datetime    not null comment '更新时间',
    update_user         varchar(64) null comment '更新人',
    index idx_rel_conversation_id_create_time (rel_conversation_id, create_time),
    index idx_rel_user_id (rel_user_id)
) comment '用户会话记忆';

create table user_knowledge
(
    id          bigint       not null comment 'ID' primary key,
    name        varchar(64)  not null comment '名称',
    description varchar(512) not null comment '描述',
    rel_user_id bigint       not null comment '关联用户ID',
    create_time datetime     not null comment '创建时间',
    create_user varchar(64)  null comment '创建人',
    update_time datetime     not null comment '更新时间',
    update_user varchar(64)  null comment '更新人',
    index idx_rel_user_id (rel_user_id)
) comment '用户-知识库';

create table user_knowledge_file
(
    id                     bigint       not null comment 'ID' primary key,
    file_name              varchar(64)  not null comment '文件名',
    description            varchar(512) null comment '文件描述',
    status                 varchar(32)  not null comment '文件状态',
    rel_user_id            bigint       not null comment '关联用户ID',
    rel_knowledge_id       bigint       null comment '关联知识库ID',
    rel_embedding_model_id bigint       null comment '关联嵌入模型ID',
    create_time            datetime     not null comment '创建时间',
    create_user            varchar(64)  null comment '创建人',
    update_time            datetime     not null comment '更新时间',
    update_user            varchar(64)  null comment '更新人',
    index idx_rel_knowledge_id (rel_knowledge_id),
    index idx_rel_user_id (rel_user_id)
) comment '用户-知识库文件';

create table user_knowledge_file_detail
(
    id          bigint      not null comment 'ID' primary key,
    document_id varchar(64) not null comment '文档ID',
    content     text        not null comment '内容',
    meta_data   json        null comment '元数据',
    rel_file_id bigint      not null comment '关联文件ID',
    create_time datetime    not null comment '创建时间',
    create_user varchar(64) null comment '创建人',
    update_time datetime    not null comment '更新时间',
    update_user varchar(64) null comment '更新人',
    index idx_rel_file_id (rel_file_id)
) comment '用户-知识库文件详情';
