//package com.wh.wenniao;
//
//import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
//import com.wh.wenniao.entity.Comment;
//import com.wh.wenniao.entity.DiscussPost;
//import com.wh.wenniao.entity.Vote;
//import com.wh.wenniao.mapper.CommentMapper;
//import com.wh.wenniao.mapper.DiscussPostMapper;
//import com.wh.wenniao.service.DiscussPostService;
//import com.wh.wenniao.service.VoteService;
//import com.wh.wenniao.util.CommunityUtil;
//import com.wh.wenniao.util.MailClient;
//import com.wh.wenniao.util.RedisKeyUtil;
//import org.junit.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.redis.core.RedisTemplate;
//
//import javax.annotation.Resource;
//import java.util.Date;
//import java.util.LinkedHashMap;
//import java.util.List;
//
//@SpringBootTest
//class WenniaoApplicationTests {
//    @Resource
//    private MailClient mailClient;
//    @Resource
//    private DiscussPostMapper discussPostMapper;
//    @Resource
//    private DiscussPostService discussPostService;
//    @Resource
//    private RedisTemplate redisTemplate;
//    @Resource
//    private VoteService voteService;
//
//    @Resource
//    private CommentMapper commentMapper;
//    @Test
//    void contextLoads() {
//        mailClient.sendMail("1163240211@qq.com","hello","mail");
//    }
//
//    @Test
//    void t(){
//        System.out.println(discussPostService.findDiscussPostRows(0,0));
//    }
//
//    @Test
//    void t2(){
//        List<DiscussPost> l = discussPostService.findDiscussPosts(0,0,10,0,0);
//        System.out.println(l);
//    }
//    @Test
//    void t3(){
//        Vote v = new Vote();
//        v.setVoteTitle("标题");
//        v.setUserCount(0);
//        LinkedHashMap<String,Integer> map = new LinkedHashMap<>();
//        map.put("选项一",0);
//        map.put("2",0);
//        v.setOp(map);
//        String id = CommunityUtil.generateUUID();
//        redisTemplate.opsForValue().set(RedisKeyUtil.getVoteKey("1"),v);
//    }
//    @Test
//    void t4(){
//        voteService.incr("c765f530bd9c45578ba4018199bf9a8c",103,"1");
//    }
//    @Test
//    void t5() {
//        Date cur = new Date();
//        System.out.println(cur);
//
//        Boolean is = redisTemplate.opsForSet().isMember(RedisKeyUtil.getVotedKey("cc3e2821c8744292ad635538cb961a7d"),103);
//        System.out.println(is);
//    }
//    @Test
//    void t6(){
//        QueryWrapper<Comment> qw = new QueryWrapper<>();
//        qw.eq("entity_id",42);
//        int cnt = commentMapper.selectCount(qw);
//        System.out.println(cnt);
//        System.out.println();
//    }
//}