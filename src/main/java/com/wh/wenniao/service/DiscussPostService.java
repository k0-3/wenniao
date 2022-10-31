package com.wh.wenniao.service;

import com.wh.wenniao.entity.DiscussPost;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface DiscussPostService{
    List<DiscussPost> findDiscussPosts(int userId,int offset,int limit,int orderMode,int tag);
    int findDiscussPostRows(int userId,int tag);
    int addDiscussPost(DiscussPost discussPost);
    DiscussPost findDiscussPostById(int id);
    int updateCommentCount(int id,int commentCount);
    int updateType(int id,int type);
    int updateStatus(int id,int status);
    int updateScore(int id,double score);
    List<DiscussPost> findMyPosts(int userId,int offset,int limit);
    int findMyPostRows(int userId);
}
