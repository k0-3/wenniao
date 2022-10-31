package com.wh.wenniao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wh.wenniao.entity.Event;
import com.wh.wenniao.entity.LoginTicket;
import com.wh.wenniao.entity.User;
import com.wh.wenniao.event.EventProducer;
import com.wh.wenniao.mapper.UserMapper;
import com.wh.wenniao.service.UserService;
import com.wh.wenniao.util.CommunityConstant;
import com.wh.wenniao.util.CommunityUtil;
import com.wh.wenniao.util.MailClient;
import com.wh.wenniao.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService, CommunityConstant {
    @Resource
    private UserMapper userMapper;

    @Resource
    private MailClient mailClient;

    //域名
    @Value("http://localhost:8081")
    private String domain;

    // 项目名(访问路径)
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Resource
    private TemplateEngine templateEngine;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private EventProducer eventProducer;

    /**
     * 注册
     * @param user
     * @return map为空说明注册成功
     */
    @Override
    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();

        if (user == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空");
            return map;
        }
        if(StringUtils.isBlank(user.getNickname())){
            map.put("nicknameMsg","昵称不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空");
            return map;
        }

        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空");
            return map;
        }

        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username",user.getUsername());
        User u = userMapper.selectOne(wrapper);
        // 验证账号是否已存在
        if (u != null) {
            map.put("usernameMsg", "该账号已存在");
            return map;
        }

        // 验证邮箱是否已存在
        wrapper.eq("email",user.getEmail());
        u = userMapper.selectOne(wrapper);
        if (u != null) {
            map.put("emailMsg", "该邮箱已被注册");
            return map;
        }

        // 注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5)); // salt
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt())); // 加盐加密
        user.setType(0); // 默认普通用户
        user.setStatus(0); // 默认未激活
        user.setActivationCode(CommunityUtil.generateUUID()); // 激活码
        user.setCreateTime(new Date()); // 注册时间
        userMapper.insert(user); //添加用户

        //消息队列发邮件
        Event event = new Event();
        event.setTopic(TOPIC_MAIL);
        event.setUserId(user.getId());
        event.setEntityId(0);
        event.setEntityType(0);
        event.setEntityUserId(user.getId());
        eventProducer.fireEvent(event);
        return map;
    }

    /**
     * 激活用户
     * @param userId
     * @param code
     * @return
     */
    @Override
    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            // 用户已激活
            return ACTIVATION_REPEAT;
        }
        else if (user.getActivationCode().equals(code)) {
            // 修改用户状态为已激活
            userMapper.updateStatus(userId, 1);
            clearCache(userId); // 用户信息变更，清除缓存中的旧数据
            return ACTIVATION_SUCCESS;
        }
        else {
            return ACTIVATION_FAILURE;
        }
    }

    @Override
    public Map<String, Object> login(String username, String password) {
        Map<String,Object> map = new HashMap<>();
        if(StringUtils.isBlank(username)){
            map.put("usernameMsg","账号不能为空");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空");
            return map;
        }
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username",username);
        User user = userMapper.selectOne(wrapper);
        if(user==null){
            map.put("usernameMsg","账号不存在");
            return map;
        }
        if(user.getStatus()==0){
            map.put("usernameMsg","账号未激活");
            return map;
        }
        password = CommunityUtil.md5(password+user.getSalt());
        if(!user.getPassword().equals(password)){
            map.put("passwordMsg","密码错误");
            return map;
        }
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0); //凭证状态，0有效，1无效
        loginTicket.setExpired(new Date(System.currentTimeMillis()+REMEMBER_EXPIRED_SECONDS*1000));//凭证到期时间

        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey,loginTicket);

        map.put("ticket",loginTicket.getTicket());

        return map;
    }
    /**
     * 根据 ticket 查询 LoginTicket 信息
     * @param ticket
     * @return
     */
    @Override
    public LoginTicket findLoginTicket(String ticket) {
        // return loginTicketMapper.selectByTicket(ticket);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }

    /**
     * 获取某个用户的权限
     * @param userId
     * @return
     */
    public Collection<? extends GrantedAuthority> getAuthorities(int userId) {
        User user = userMapper.selectById(userId);
        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()) {
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;
    }
    /**
     * 缓存中没有该用户信息时，则将其存入缓存
     * @param userId
     * @return
     */
    private User initCache(int userId) {
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }

    /**
     * 优先从缓存中取值
     * @param userId
     * @return
     */
    private User getCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }

    /**
     * 用户信息变更时清除对应缓存数据
     * @param userId
     */
    private void clearCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }
}
