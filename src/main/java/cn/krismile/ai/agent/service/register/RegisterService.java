package cn.krismile.ai.agent.service.register;

import cn.krismile.ai.agent.model.request.register.RegisterByUsernameRequest;

/**
 * 注册服务
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
public interface RegisterService {

    /**
     * 通过用户名注册
     *
     * @param request 注册参数
     * @return token
     * @since 1.0.0
     */
    String registerByUsername(RegisterByUsernameRequest request);

    /**
     * 判断用户名是否已注册
     *
     * @param username 用户名
     * @return true 已注册，false 未注册
     * @since 1.0.0
     */
    boolean isRegistered(String username);

}