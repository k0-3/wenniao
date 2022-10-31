package com.wh.wenniao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wh.wenniao.entity.Comment;
import com.wh.wenniao.mapper.CommentMapper;
import com.wh.wenniao.mapper.DiscussPostMapper;
import com.wh.wenniao.service.CommentService;
import com.wh.wenniao.util.SensitiveFilter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;

@Service
public class CommentServiceImpl implements CommentService {
    @Resource
    private SensitiveFilter sensitiveFilter;
    @Resource
    private CommentMapper commentMapper;
    @Resource
    private DiscussPostMapper discussPostMapper;

    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    public int addComment(Comment comment) {
        if(comment==null){
            throw new IllegalArgumentException("参数不能为空");
        }
        //转义html
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));

        //过滤敏感词
        comment.setContent(sensitiveFilter.filter(comment.getContent()));

        int res = commentMapper.insert(comment);

        QueryWrapper<Comment> qw = new QueryWrapper<>();
        qw.eq("entity_id",comment.getEntityId());
        int cnt = commentMapper.selectCount(qw);
        discussPostMapper.updateCommentCount(comment.getEntityId(),cnt);
        return res;
    }
}
