package cn.krismile.ai.agent.structure.authorization;

import cn.dev33.satoken.stp.StpInterface;

import java.util.List;

/**
 * StpInterfaceImpl
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
public class StpInterfaceImpl implements StpInterface {

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return List.of();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return List.of();
    }
}
