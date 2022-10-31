package com.wh.wenniao.controller;

import com.wh.wenniao.entity.Comment;
import com.wh.wenniao.entity.DiscussPost;
import com.wh.wenniao.entity.Event;
import com.wh.wenniao.entity.User;
import com.wh.wenniao.event.EventProducer;
import com.wh.wenniao.mapper.DiscussPostMapper;
import com.wh.wenniao.service.CommentService;
import com.wh.wenniao.util.CommunityConstant;
import com.wh.wenniao.util.CommunityUtil;
import com.wh.wenniao.util.HostHolder;
import com.wh.wenniao.util.RedisKeyUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {

    @Resource
    private HostHolder hostHolder;
    @Resource
    private DiscussPostMapper discussPostMapper;
    @Resource
    private CommentService commentService;
    @Resource
    private EventProducer eventProducer;
    @Resource
    private RedisTemplate redisTemplate;

    @GetMapping("/publish/{postId}")
    public String getCommentPage(Model model,
                                 @PathVariable(name="postId")Integer postId,
                                 @RequestParam(name="voteId",defaultValue = "-1")String voteId,
                                 @RequestParam(name="diceId",defaultValue = "-1")Integer diceId){
        model.addAttribute("postId",postId);
        model.addAttribute("voteId",voteId);
        model.addAttribute("diceId",diceId);
        return "/site/discuss-comment";
    }

    @GetMapping("/vote/{postId}")
    public String getVotePage(@PathVariable("postId")Integer postId,
                              Model model){
        model.addAttribute("postId",postId);
        return "/site/c-vote";}

    @GetMapping("/dice/{postId}")
    public String getDicePage(@PathVariable("postId")Integer postId,
                              Model model){
        model.addAttribute("postId",postId);
        return "/site/c-dice";
    }

    @PostMapping("/add")
    @ResponseBody
    public String publish(@RequestParam(name="nickname",required = false)String nickname,
                            @NotEmpty(message = "正文不能为空") String content,
                          @RequestParam("postId") int postId,
                          @RequestParam(name="voteId",required = false) String voteid,
                          @RequestParam(name="diceId",required = false) Integer diceid){
        User user = hostHolder.getUser();
        if(user==null){
            CommunityUtil.getJSONString(403,"您还未登录");
        }
        DiscussPost discussPost = discussPostMapper.selectById(postId);
        Comment comment = new Comment();
        comment.setUserId(user.getId());
        comment.setEntityType(1);//帖子
        comment.setEntityId(postId);
        comment.setTargetId(discussPost.getUserId());
        comment.setContent(content);
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        if(nickname=="" ||nickname==null || nickname.equals("")){
            comment.setNickname(user.getNickname());
        }
        else{
            comment.setNickname(nickname);
        }
        if(voteid==null || voteid.equals(""))
             voteid = null;
        comment.setVoteId(voteid);
        comment.setDiceId(diceid);

        commentService.addComment(comment);

        //评论事件 存入elasticsearch+刷新帖子分数
        Event event = new Event();
        event.setTopic(TOPIC_COMMNET);
        event.setUserId(user.getId());
        event.setEntityId(comment.getEntityId());
        event.setEntityType(comment.getEntityType());
        event.setData("postId",postId);
        event.setEntityUserId(discussPost.getUserId());
        eventProducer.fireEvent(event);

        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey,postId);

        return CommunityUtil.getJSONString(0,"评论成功");
    }
}
