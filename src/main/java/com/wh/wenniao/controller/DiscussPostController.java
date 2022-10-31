package com.wh.wenniao.controller;

import com.wh.wenniao.entity.*;
import com.wh.wenniao.event.EventProducer;
import com.wh.wenniao.mapper.CommentMapper;
import com.wh.wenniao.mapper.DiceMapper;
import com.wh.wenniao.service.DiscussPostService;
import com.wh.wenniao.service.UserService;
import com.wh.wenniao.service.VoteService;
import com.wh.wenniao.util.CommunityConstant;
import com.wh.wenniao.util.CommunityUtil;
import com.wh.wenniao.util.HostHolder;
import com.wh.wenniao.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import java.io.File;
import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Resource
    private DiscussPostService discussPostService;

    @Resource
    private UserService userService;

    @Resource
    private HostHolder hostHolder;

    @Resource
    private EventProducer eventProducer;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private DiceMapper diceMapper;

    // 网站域名
    @Value("http://192.168.3.17:8081")
    private String domain;

    // 项目名(访问路径)
    @Value("")
    private String contextPath;

    // editorMd 图片上传地址
    @Value("D:\\code\\java\\wenniao\\src\\main\\resources\\static\\editor-md-upload")
    private String editormdUploadPath;

    @Resource
    private VoteService voteService;

    @Resource
    private CommentMapper commentMapper;

    /**
     *
     * @param discussPostId
     * @param model
     * @param page
     * @param author  0-查看全部 1-只看楼主
     * @return
     */
    @GetMapping("/detail/{discussPostId}")
    public String getDetailPage(@PathVariable("discussPostId") int discussPostId,
                                Model model,
                                Page page,
                                @RequestParam(name="author",defaultValue = "0")int author){
        //帖子
        DiscussPost discussPost = discussPostService.findDiscussPostById(discussPostId);
        String content = HtmlUtils.htmlUnescape(discussPost.getContent());//反转义进行markdown展示
        discussPost.setContent(content);

        model.addAttribute("post",discussPost);

        //作者
        User user =userService.getById(discussPost.getUserId());
        model.addAttribute("user",user);

        //是否存在投票
        Boolean isvote = discussPost.getVoteId()!=null && !discussPost.getVoteId().equals("");
        model.addAttribute("isvote",isvote);

        //投票
        Vote vote = voteService.findVoteById(discussPost.getVoteId());
        model.addAttribute("vote",vote);

        //投过票或者过期
        Date cur = new Date();
        Boolean isvoted = voteService.isVoted(discussPost.getVoteId(),hostHolder.getUser().getId());
        if(discussPost.getVoteId()!=null && !discussPost.getVoteId().equals("") && cur.compareTo(vote.getExpire())>0){
            isvoted = true;
        }
        model.addAttribute("isvoted",isvoted);

        Dice dice = diceMapper.selectById(discussPost.getDiceId());
        model.addAttribute("dice",dice);

        //评论
        List<Comment> list = commentMapper.selectComments(1,discussPostId,page.getOffset(),page.getLimit(),author);
        List<Map<String, Object>> commentList = new ArrayList<>();
        if(list!=null){
            for (Comment comment : list) {
                Map<String, Object> map = new HashMap<>();
                map.put("comment", comment);
                //是否存在投票
                isvote = comment.getVoteId() != null && !comment.getVoteId().equals("");
                map.put("isvote", isvote);
                //投票
                vote = voteService.findVoteById(comment.getVoteId());
                map.put("vote", vote);
                //投过票或者过期
                cur = new Date();
                isvoted = voteService.isVoted(comment.getVoteId(), hostHolder.getUser().getId());
                if (comment.getVoteId() != null && !comment.getVoteId().equals("") && cur.compareTo(vote.getExpire()) > 0) {
                    isvoted = true;
                }
                map.put("isvoted", isvoted);
                dice = diceMapper.selectById(comment.getDiceId());
                map.put("dice", dice);
                commentList.add(map);
            }
        }
        model.addAttribute("comments",commentList);

        //分页
        page.setLimit(100);
        page.setPath("/discuss/detail/"+discussPostId);
        page.setRows(discussPost.getCommentCount());
        model.addAttribute("page",page);
        return "/site/discuss-detail";
    }

    @GetMapping("/publish")
    public String getPublishPage(Model model,
                                 @RequestParam(name="voteId",defaultValue = "-1")String voteId,
                                 @RequestParam(name="diceId",defaultValue = "-1")Integer diceId){
        model.addAttribute("voteId",voteId);
        model.addAttribute("diceId",diceId);
        return "/site/discuss-post";
    }

    @GetMapping("/vote")
    public String getVotePage(){return "/site/vote";}

    @GetMapping("/dice")
    public String getDicePage(){return "/site/dice";}

    @PostMapping("/vote")
    @ResponseBody
    public String vote(@RequestParam(name="title")String title,
                       @RequestParam(name="list[]")String[] list){
        //前端传数组，后端的name是list[]
        String id = voteService.vote(title,list);
        HashMap<String,Object> map = new HashMap<>();
        map.put("voteId",id);
        return CommunityUtil.getJSONString(0,"发布成功",map);
    }

    @PostMapping("/dice")
    @ResponseBody
    public String dice(@RequestParam(name="num",defaultValue = "1") Integer num,
                       @RequestParam(name="lower",defaultValue = "1")Integer lower,
                       @RequestParam(name="upper")Integer upper){
        Random r = new Random();
        StringBuilder diceString = new StringBuilder();
        int sum = 0;
        for(int i = 0;i<num;i++){
            int t = r.nextInt(upper-lower)+lower;
            sum += t;
            if(i==num-1){
                diceString.append(t+"");
            }
            else{
                diceString.append(t+"+");
            }
        }
        diceString.append("="+sum);
        Dice dice = new Dice();
        dice.setDiceString(diceString.toString());
        diceMapper.insert(dice);

        HashMap<String,Object> map = new HashMap<>();
        map.put("diceId",dice.getId());
        map.put("dice",diceString);
        return CommunityUtil.getJSONString(0,"添加骰子成功",map);
    }

    @PostMapping("/uploadMdPic")
    @ResponseBody
    public String uploadMdPic(@RequestParam(value="editormd-image-file",required = false)MultipartFile file){
        String url = null; // 图片访问地址
        try {
            // 获取上传文件的名称
            String trueFileName = file.getOriginalFilename();
            String suffix = trueFileName.substring(trueFileName.lastIndexOf("."));
            String fileName = CommunityUtil.generateUUID() + suffix;

            // 图片存储路径
            File dest = new File(editormdUploadPath + "/" + fileName);
            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs();
            }
            // 保存图片到存储路径
            file.transferTo(dest);
            // 图片访问地址
            url = domain  + "/editor-md-upload/" + fileName;
        } catch (Exception e) {
            e.printStackTrace();
            return CommunityUtil.getEditorMdJSONString(0, "上传失败", url);
        }
        return CommunityUtil.getEditorMdJSONString(1, "上传成功", url);
    }


    @PostMapping("/add")
    @ResponseBody
    public String addDiscussPost(@NotEmpty(message = "标题不能为空")String title,
                                 @NotEmpty(message = "正文不能为空") String content, int tag,
                                 @RequestParam(name="voteId",required = false) String voteid,
                                 @RequestParam(name="diceId",required = false) Integer diceid){
        User user = hostHolder.getUser();
        if(user==null){
            return CommunityUtil.getJSONString(403,"您还未登录");
        }
        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setType(0);
        discussPost.setStatus(0);
        discussPost.setCreateTime(new Date());
        discussPost.setLatestComment(new Date());
        discussPost.setCommentCount(0);
        if(voteid==null || voteid.equals(""))
            voteid = null;
        discussPost.setVoteId(voteid);
        discussPost.setDiceId(diceid);
        discussPost.setTag(tag);

        discussPostService.addDiscussPost(discussPost);

        //消息队列 存到es服务器中
        Event event = new Event();
        event.setTopic(TOPIC_PUBLISH);
        event.setUserId(user.getId());
        event.setEntityType(ENTITY_TYPE_POST);
        event.setEntityId(discussPost.getId());
        eventProducer.fireEvent(event);

        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey,discussPost.getId());

        return CommunityUtil.getJSONString(0,"发布成功");
    }

    @PostMapping("/voting")
    @ResponseBody
    public String voting(@RequestParam("voteId") String voteId,
                         @RequestParam("userId") Integer userId,
                         @RequestParam("op") String op,
                         @RequestParam("isvote")Boolean isvote,
                         Model model){
        voteService.incr(voteId,userId,op);
        boolean isvoted = true;
        model.addAttribute("isvoted",isvoted);
        model.addAttribute("isvote",isvote);
        Vote vote = voteService.findVoteById(voteId);
        model.addAttribute("vote",vote);
        return CommunityUtil.getJSONString(0,"投票成功");
    }


    @PostMapping("/delvote/{voteId}")
    public String delVote(@PathVariable("voteId")String voteId,Model model){
        redisTemplate.delete(RedisKeyUtil.getVoteKey(voteId)); //删除key
        model.addAttribute("voteId","-1");
        return "/site/discuss-post::v_refresh";
    }

    @PostMapping("/deldice/{diceId}")
    public String delDice(@PathVariable("diceId")Integer diceId,Model model){
        diceMapper.deleteById(diceId);
        model.addAttribute("diceId",-1);
        return "/site/discuss-post::d_refresh";
    }

    @PostMapping("/over")
    @ResponseBody
    public String getOver(@RequestParam("postId")Integer postId){
        discussPostService.updateStatus(postId,1);
        return CommunityUtil.getJSONString(0,"已标为完结状态");
    }
}
