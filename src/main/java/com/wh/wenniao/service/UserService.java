package com.wh.wenniao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wh.wenniao.entity.LoginTicket;
import com.wh.wenniao.entity.User;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Map;

public interface UserService extends IService<User> {
    Map<String, Object> register(User user);
    int activation(int userId,String code);
    Map<String,Object> login(String username,String password);
    LoginTicket findLoginTicket(String ticket);
    Collection<? extends GrantedAuthority> getAuthorities(int userId);
}
