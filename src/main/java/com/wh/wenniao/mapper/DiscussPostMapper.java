package com.wh.wenniao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wh.wenniao.entity.DiscussPost;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.security.core.parameters.P;

import java.util.List;

public interface DiscussPostMapper extends BaseMapper<DiscussPost> {
    @Select("select count(id) from discuss_post where status != 2")
    int selectAllRows();

    /**
     * 计算该tag下的帖子总数
     */
    @Select("<script>" +
            "select count(id) from discuss_post where status != 2 " +
            "<if test = 'tag !=0'>" +
            " and tag=#{tag}" +
            "</if>" +
            "</script>")
    int selectTagRows(int tag);

    /**
     * 查找帖子数量
     */
    @Select("select count(id) from discuss_post where status !=2 and user_id=#{userId}")
    int selectUserPostRows(int userId);

    /**
     * 分页查询帖子
     */
    @Select("<script>" +
            "select * from discuss_post where status != 2 " +
            "<if test = 'userId != 0'> " +
            " and user_id = #{userId}" +
            "</if>" +
            "<if test = 'tag != 0'>" +
            " and tag = #{tag}" +
            "</if>" +
            "<if test = 'orderMode == 0'>" +
            " order by type desc,latest_comment desc" +
            "</if>" +
            "<if test = 'orderMode == 1'>" +
            " order by type desc,score desc, latest_comment desc" +
            "</if>" +
            "<if test = 'orderMode == 2'>" +
            " and status = 1 order by type desc,latest_comment desc" +
            "</if>" +
            " limit #{offset},#{limit}" +
            "</script>"
    )
    List<DiscussPost> selectDiscussPosts(@Param("userId")int userId, @Param("offset")int offset, @Param("limit")int limit, @Param("orderMode")int orderMode, @Param("tag")int tag);

    /**
     *增加评论数
     */
    @Update("update discuss_post set comment_count=#{commentCount} where id=#{id}")
    int updateCommentCount(@Param("id")int id,@Param("commentCount")int commentCount);

    @Update("update discuss_post set type=#{type} where id=#{id}")
    int updateType(@Param("id")int id,@Param("type")int type);

    @Update("update discuss_post set status=#{status} where id=#{id}")
    int updateStatus(@Param("id")int id,@Param("status")int status);

    @Update("update discuss_post set score=#{score} where id=#{id}")
    int updateScore(@Param("id")int id,@Param("score")double score);

    @Select("select * " +
            "from discuss_post " +
            "where user_id=#{userId} " +
            "order by create_time desc " +
            "limit #{offset},#{limit} ")
    List<DiscussPost> selectMyPosts(@Param("userId")int userId, @Param("offset")int offset, @Param("limit")int limit);

    @Select("select count(id) from discuss_post " +
            "where user_id=#{userId}")
    int selectMyPostRows(@Param("userId")int userId);
}
