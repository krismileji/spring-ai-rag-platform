package cn.krismile.ai.agent.mapper;

import cn.krismile.ai.agent.model.domain.UserDO;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * UserMapper
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Mapper
public interface UserMapper extends BaseMapper<UserDO> {
}