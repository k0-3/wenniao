package com.wh.wenniao.service.impl;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.wh.wenniao.entity.DiscussPost;
import com.wh.wenniao.mapper.DiscussPostMapper;
import com.wh.wenniao.service.DiscussPostService;
import com.wh.wenniao.util.SensitiveFilter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DiscussPostServiceImpl implements DiscussPostService {

    private static final Logger logger = LoggerFactory.getLogger(DiscussPostServiceImpl.class);

    @Resource
    private DiscussPostMapper discussPostMapper;

    @Resource
    private SensitiveFilter sensitiveFilter;

    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    //热帖缓存 key - offset：limit  起始索引：每页数据条数l,
    private LoadingCache<String,List<DiscussPost>> postListCache;

    //帖子总数缓存 key - tag value - 数量   0-全部 1-安科 2-大纲 3-rpg 4-海龟汤 5-沉浸式 6-水区
    private LoadingCache<Integer,Integer> postRowsCache;

    /**
     * 初始化Caffeine
     */
    @PostConstruct
    public void init() {
        // 初始化热帖列表缓存
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    // 如果缓存Caffeine中没有数据，告诉缓存如何去数据库中查数据，再装到缓存中
                    @Nullable
                    @Override
                    public List<DiscussPost> load(@NonNull String key) throws Exception {
                        if (key == null || key.length() == 0) {
                            throw new IllegalArgumentException("参数错误");
                        }

                        String[] params = key.split(":");
                        if (params == null || params.length != 2) {
                            throw new IllegalArgumentException("参数错误");
                        }

                        int offset = Integer.valueOf(params[0]);
                        int limit = Integer.valueOf(params[1]);

                        logger.debug("load post list from DB");
                        return discussPostMapper.selectDiscussPosts(0, offset, limit, 1,0);
                    }
                });

        // 初始化帖子总数缓存
        postRowsCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Nullable
                    @Override
                    public Integer load(@NonNull Integer key) throws Exception {
                        logger.debug("load post rows from DB");
                        return discussPostMapper.selectTagRows(key);
                    }
                });
    }

    /**
     *
     * @param userId 0-全部
     * @param offset
     * @param limit
     * @param orderMode 0-最新 1-最热
     * @param tag
     * @return
     */
    @Override
    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit, int orderMode, int tag) {
        if(userId==0 && orderMode ==1 && tag==0){
            return  postListCache.get(offset+":"+limit);
        }
        logger.debug("load post list from DB");
        return discussPostMapper.selectDiscussPosts(userId,offset,limit,orderMode,tag);
    }

    @Override
    public int findDiscussPostRows(int userId, int tag) {
        if(userId == 0){
            return postRowsCache.get(tag);
        }
        logger.debug("load post rows from DB");
        return discussPostMapper.selectUserPostRows(userId);
    }

    /**
     * 增加帖子
     * @param discussPost
     * @return
     */
    @Override
    public int addDiscussPost(DiscussPost discussPost) {
        if(discussPost == null){
            throw new IllegalArgumentException("参数不能为空");
        }
        // 转义HTML标记，防止在html标签注入攻击语句
        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));

        // 过滤敏感词
        discussPost.setTitle(sensitiveFilter.filter(discussPost.getTitle()));
        discussPost.setContent(sensitiveFilter.filter(discussPost.getContent()));
        return discussPostMapper.insert(discussPost);
    }

    @Override
    public DiscussPost findDiscussPostById(int id) {
        return discussPostMapper.selectById(id);
    }

    /**
     * 增加评论数量
     * @param id
     * @param commentCount
     * @return
     */
    @Override
    public int updateCommentCount(int id, int commentCount) {
        return discussPostMapper.updateCommentCount(id,commentCount);
    }

    @Override
    public int updateType(int id, int type) {
        return discussPostMapper.updateType(id,type);
    }

    @Override
    public int updateStatus(int id, int status) {
        return discussPostMapper.updateStatus(id,status);
    }

    @Override
    public int updateScore(int id, double score) {
        return discussPostMapper.updateScore(id,score);
    }

    @Override
    public List<DiscussPost> findMyPosts(int userId, int offset, int limit) {
        List<DiscussPost> list = discussPostMapper.selectMyPosts(userId,offset,limit);
        return list;
    }

    @Override
    public int findMyPostRows(int userId) {
        return discussPostMapper.selectMyPostRows(userId);
    }

}
