package com.wh.wenniao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wh.wenniao.entity.Comment;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CommentMapper extends BaseMapper<Comment> {
    @Select("<script>" +
            "select * from comment " +
            "where status = 0 " +
            "and entity_type=#{entityType} " +
            "and entity_id = #{entityId} " +
            "<if test= 'author!=0'>" +
            " and user_id = target_id" +
            "</if>" +
            "order by create_time asc " +
            "limit #{offset},#{limit} " +
            "</script>")
    List<Comment> selectComments(@Param("entityType") int entityType, @Param("entityId") int entityId,
                                 @Param("offset") int offset, @Param("limit") int limit,@Param("author")int author);
}
