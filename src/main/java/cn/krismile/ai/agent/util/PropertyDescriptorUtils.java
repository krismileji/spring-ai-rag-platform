package cn.krismile.ai.agent.util;

import lombok.SneakyThrows;
import org.apache.commons.beanutils.BeanUtils;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

/**
 * PropertyDescriptorUtils
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
public class PropertyDescriptorUtils {

    /**
     * 忽略源对象中的 null 值
     *
     * @param source 源对象
     * @param target 目标对象
     * @since 1.0.0
     */
    @SneakyThrows
    public static void copyPropertiesIgnoreNull(Object source, Object target) {
        BeanInfo beanInfo = Introspector.getBeanInfo(source.getClass());
        for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {
            if (descriptor.getReadMethod() != null && descriptor.getWriteMethod() != null) {
                Object value = descriptor.getReadMethod().invoke(source);
                if (value != null) {
                    try {
                        BeanUtils.copyProperty(target, descriptor.getName(), value);
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }
}
