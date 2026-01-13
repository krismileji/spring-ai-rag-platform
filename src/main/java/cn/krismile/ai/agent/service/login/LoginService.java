package cn.krismile.ai.agent.service.login;

import cn.krismile.ai.agent.model.request.login.LoginByUsernameRequest;

/**
 * 登录服务
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
public interface LoginService {

    /**
     * 通过用户名登录
     *
     * @param request 登录参数
     * @return token
     * @since 1.0.0
     */
    String loginByUsername(LoginByUsernameRequest request);;

}