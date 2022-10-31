package com.wh.wenniao.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wh.wenniao.entity.Collect;
import com.wh.wenniao.entity.DiscussPost;
import com.wh.wenniao.entity.Page;
import com.wh.wenniao.entity.User;
import com.wh.wenniao.mapper.CollectMapper;
import com.wh.wenniao.service.DiscussPostService;
import com.wh.wenniao.service.UserService;
import com.wh.wenniao.util.CommunityUtil;
import com.wh.wenniao.util.HostHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class UserController {
    @Resource
    private HostHolder hostHolder;
    @Resource
    private DiscussPostService discussPostService;
    @Resource
    private UserService userService;
    @Resource
    private CollectMapper collectMapper;

    @PostMapping("/collect")
    @ResponseBody
    public String collect(@RequestParam("postId")int postId){
        User user = hostHolder.getUser();
        if(user==null){
            return CommunityUtil.getJSONString(403,"未登录");
        }
        Collect c = new Collect();
        c.setUserId(user.getId());
        c.setPostId(postId);
        collectMapper.insert(c);
        return CommunityUtil.getJSONString(0,"收藏成功");
    }

    @GetMapping("/mycollection")
    public String getMyCollecitonPage(Page page, Model model){
        User user = hostHolder.getUser();
        if(user==null){
            return "/index";
        }
        QueryWrapper<Collect> qw = new QueryWrapper<>();
        qw.eq("user_id",user.getId());
        List<Collect> list = collectMapper.selectList(qw);
        List<Map<String,Object>> discussPosts = new ArrayList<>();
        if(list!=null){
            for(Collect c:list){
                Map<String,Object> map = new HashMap<>();
                DiscussPost post = discussPostService.findDiscussPostById(c.getPostId());
                map.put("post",post);
                user = userService.getById(post.getUserId());
                map.put("user",user);
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts",discussPosts);
        return "/site/my-collection";
    }

    @GetMapping("/mypost")
    public String getMyPostPage(Page page,Model model){
        User user = hostHolder.getUser();
        if(user==null){
            return "/index";
        }
        List<DiscussPost> list = discussPostService.findMyPosts(user.getId(),page.getOffset(),page.getLimit());
        List<Map<String,Object>> discussPosts = new ArrayList<>();
        if(list !=null){
            for(DiscussPost post:list){
                Map<String,Object> map = new HashMap<>();
                map.put("post",post);
                user = userService.getById(post.getUserId());
                map.put("user",user);
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts",discussPosts);

        page.setRows(discussPostService.findMyPostRows(user.getId()));
        page.setPath("/mypost");
        model.addAttribute("page",page);
        return "/site/my-post";
    }
}
