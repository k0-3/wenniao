package com.wh.wenniao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wh.wenniao.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface UserMapper extends BaseMapper<User> {
    @Update("update user set status = #{status} where id = #{userId}")
    int updateStatus(@Param("userId") int userId,@Param("status") int status);
}
