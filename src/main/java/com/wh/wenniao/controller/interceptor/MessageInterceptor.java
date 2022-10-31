package com.wh.wenniao.controller.interceptor;

import com.wh.wenniao.entity.User;
import com.wh.wenniao.util.HostHolder;
import org.apache.kafka.common.security.oauthbearer.internals.unsecured.OAuthBearerUnsecuredJws;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MessageInterceptor implements HandlerInterceptor {
    @Resource
    private HostHolder hostHolder;
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object Handler, ModelAndView modelAndView) throws Exception{
        User user = hostHolder.getUser();
        if(user!=null && modelAndView !=null){

        }
    }
}
