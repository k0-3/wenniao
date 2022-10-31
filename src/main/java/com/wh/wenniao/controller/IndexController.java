package com.wh.wenniao.controller;


import com.wh.wenniao.entity.DiscussPost;
import com.wh.wenniao.entity.Page;
import com.wh.wenniao.entity.User;
import com.wh.wenniao.mapper.UserMapper;
import com.wh.wenniao.service.DiscussPostService;
import com.wh.wenniao.service.UserService;
import com.wh.wenniao.service.impl.ElasticsearchService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 首页
 */
@Controller
public class IndexController {

    @Resource
    private DiscussPostService discussPostService;

    @Resource
    private UserService userService;

    @Resource
    private UserMapper userMapper;

    @Resource
    private ElasticsearchService elasticsearchService;

    /**
     * 主页
     * @param model
     * @param orderMode 0-最新 1-最热 2-完结
     * @param tag 0-全部 1-安科 2-大纲 3-rpg 4-海龟汤 5-沉浸式 6-水区
     * @return
     */
    @GetMapping({"/index","/"})
    public String getIndexPage(Model model,
                               @RequestParam(name="orderMode",defaultValue = "0") int orderMode,
                               @RequestParam(name="tag",defaultValue = "0") int tag,
                               Page page){
        page.setRows(discussPostService.findDiscussPostRows(0,tag));
        page.setPath("/index?orderMode="+orderMode+"&tag="+tag);
        model.addAttribute("page",page);
        List<DiscussPost> list = discussPostService.findDiscussPosts(0,page.getOffset(),page.getLimit(),orderMode,tag);
        //帖子和用户信息
        List<Map<String,Object>> discussPosts = new ArrayList<>();
        if(list !=null){
            for(DiscussPost post:list){
                Map<String,Object> map = new HashMap<>();
                map.put("post",post);
                User user = userService.getById(post.getUserId());
                map.put("user",user);
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts",discussPosts);
        model.addAttribute("orderMode",orderMode);
        model.addAttribute("tag",tag);
        return "/index";
    }


    @PostMapping("/search")
    public String getSearchPage(@RequestParam("keyword") String keyword,Page page,Model model){
        // 搜索帖子 (Spring 提供的 Page 当前页码从 0 开始计数)
        org.springframework.data.domain.Page<DiscussPost> searchResult =
                elasticsearchService.searchDiscussPost(keyword, page.getCurrent()-1, page.getLimit());
        // 聚合数据
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (searchResult != null) {
            for (DiscussPost post : searchResult) {
                Map<String, Object> map = new HashMap<>();
                // 帖子
                map.put("post", post);
                // 作者
                map.put("user", userMapper.selectById(post.getUserId()));
                discussPosts.add(map);
                System.out.println(post);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("keyword", keyword);

        // 设置分页
        page.setPath("/search?keyword="+ keyword);
        page.setRows(searchResult == null ? 0 : (int) searchResult.getTotalElements());
        return "/site/search";
    }

    @GetMapping("/error")
    public String getErrorPage(){
        return "/error/500";
    }


    @GetMapping("/t2")
    public String t2(){return "/test2";}

    @GetMapping("/t1")
    public String t1(){return "/test1";}

    @GetMapping("/t3")
    public String t3(){return "/test3";}

    @PostMapping("/t3")
    public String t33(Model m){
        m.addAttribute("dice",2);
        return "/test2";
    }

    @PostMapping("/t1")
    public String t11(Model m){
        System.out.println("进入.....");
        m.addAttribute("vote",1);
        return "/test2";
    }

    @PostMapping("/t2")
    public String t22(){return "redirect:/index";}
}
