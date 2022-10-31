package com.wh.wenniao.service.impl;

import com.wh.wenniao.entity.Vote;
import com.wh.wenniao.service.VoteService;
import com.wh.wenniao.util.CommunityUtil;
import com.wh.wenniao.util.RedisKeyUtil;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;

@Service
public class VoteServiceImpl implements VoteService {
    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 生成投票
     * @param title
     * @param list
     */
    @Override
    public String vote(String title, String[] list) {
        Vote vote = new Vote();
        vote.setVoteTitle(title);

        Date date = new Date();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        // 把日期往后增加一天,整数  往后推,负数往前移动
        calendar.add(Calendar.DATE, 1);
        // 这个时间就是日期往后推一天的结果
        date = calendar.getTime();

        vote.setExpire(date);
        vote.setUserCount(0);
        LinkedHashMap<String,Integer> map = new LinkedHashMap<>();
        for(int i = 0;i<list.length;i++){
            map.put(list[i],0);
        }
        vote.setOp(map);
        String id = CommunityUtil.generateUUID();//生成vote id
        redisTemplate.opsForValue().set(RedisKeyUtil.getVoteKey(id),vote); // 放入redis
        return id;
    }

    @Override
    public void incr(String voteId, Integer userId,String op) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                //修改选项值
                Vote vote = (Vote)redisTemplate.opsForValue().get(RedisKeyUtil.getVoteKey(voteId));
                LinkedHashMap<String,Integer> map = vote.getOp();
                map.put(op,map.get(op)+1);
                vote.setOp(map);
                vote.setUserCount(vote.getUserCount()+1);

                redisOperations.multi(); // 开启事务

                redisTemplate.opsForValue().set(RedisKeyUtil.getVoteKey(voteId),vote);//写回redis

                //放入voted里面
                redisTemplate.opsForSet().add(RedisKeyUtil.getVotedKey(voteId),userId);

                return redisOperations.exec();//提交事务
            }
        });
    }

    @Override
    public Vote findVoteById(String id) {
        Vote vote = (Vote)redisTemplate.opsForValue().get(RedisKeyUtil.getVoteKey(id));
        return vote;
    }

    @Override
    public Boolean isVoted(String id, Integer userId) {
        Boolean isvoted = redisTemplate.opsForSet().isMember(RedisKeyUtil.getVotedKey(id),userId);
        return isvoted;
    }
}
