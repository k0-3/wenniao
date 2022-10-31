package com.wh.wenniao.service;

import com.wh.wenniao.entity.Vote;

public interface VoteService {
    /**
     * 发布投票
     * @param title 投票标题
     * @param list 投票选项
     * @return
     */
    String vote(String title,String[] list);

    /**
     * 增加投票
     * @param voteId 投票id
     * @param userId 投票者
     * @param op 所投选项
     */
    void incr(String voteId,Integer userId,String op);
    Vote findVoteById(String id);
    Boolean isVoted(String id,Integer userId);
}
