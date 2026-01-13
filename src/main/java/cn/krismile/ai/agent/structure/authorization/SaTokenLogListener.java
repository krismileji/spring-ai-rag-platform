package cn.krismile.ai.agent.structure.authorization;

import cn.dev33.satoken.listener.SaTokenListener;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import host.springboot.framework3.core.logging.LoggingComponent;
import host.springboot.framework3.core.model.Pair;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * SaTokenLogListener
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
public class SaTokenLogListener implements SaTokenListener, LoggingComponent {

    @Override
    public void doLogin(String loginType, Object loginId, String tokenValue, SaLoginParameter loginParameter) {
        logInstance().info("登录成功", List.of(
                Pair.of("loginType", loginType),
                Pair.of("loginId", loginId)
        ));
    }

    @Override
    public void doLogout(String loginType, Object loginId, String tokenValue) {
        logInstance().info("退出登录", List.of(
                Pair.of("loginType", loginType),
                Pair.of("loginId", loginId)
        ));
    }

    @Override
    public void doKickout(String loginType, Object loginId, String tokenValue) {
        logInstance().info("被踢下线", List.of(
                Pair.of("loginType", loginType),
                Pair.of("loginId", loginId)
        ));
    }

    @Override
    public void doReplaced(String loginType, Object loginId, String tokenValue) {
        logInstance().info("被顶下限", List.of(
                Pair.of("loginType", loginType),
                Pair.of("loginId", loginId)
        ));
    }

    @Override
    public void doDisable(String loginType, Object loginId, String service, int level, long disableTime) {
        logInstance().info("封禁用户", List.of(
                Pair.of("loginType", loginType),
                Pair.of("loginId", loginId),
                Pair.of("service", service),
                Pair.of("level", level),
                Pair.of("disableTime", disableTime)
        ));
    }

    @Override
    public void doUntieDisable(String loginType, Object loginId, String service) {
        logInstance().info("解封用户", List.of(
                Pair.of("loginType", loginType),
                Pair.of("loginId", loginId),
                Pair.of("service", service)
        ));
    }

    @Override
    public void doOpenSafe(String loginType, String tokenValue, String service, long safeTime) {
        logInstance().info("打开二级认证", List.of(
                Pair.of("loginType", loginType),
                Pair.of("service", service),
                Pair.of("safeTime", safeTime)
        ));
    }

    @Override
    public void doCloseSafe(String loginType, String tokenValue, String service) {
        logInstance().info("关闭二级认证", List.of(
                Pair.of("loginType", loginType),
                Pair.of("service", service)
        ));
    }

    @Override
    public void doCreateSession(String id) {
        logInstance().info("创建 Session", Pair.of("id", id));
    }

    @Override
    public void doLogoutSession(String id) {
        logInstance().info("注销 Session", Pair.of("id", id));
    }

    @Override
    public void doRenewTimeout(String loginType, Object loginId, String tokenValue, long timeout) {
        logInstance().info("Token 续期",List.of(
                Pair.of("loginType", loginType),
                Pair.of("loginId", loginId),
                Pair.of("timeout", timeout)
        ));
    }

    @Override
    public @NonNull String logTag() {
        return "Authorization-Listener";
    }
}
