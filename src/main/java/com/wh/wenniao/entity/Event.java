package com.wh.wenniao.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
public class Event implements Serializable {
    private String topic; //主题 ：回复  关注  邮件
    private int userId; //触发者
    private int entityType; // 实体类型  1帖子 2评论  关注邮件0
    private int entityId; // 实体id
    private int entityUserId; // 实体用户 通知发送给他
    private Map<String,Object> data = new HashMap<>();
    public Event setData(String key, Object value) {
        this.data.put(key, value);
        return this;
    }
}
