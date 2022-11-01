package com.wh.wenniao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class User implements Serializable {
    @TableId(type= IdType.AUTO)
    Integer id;
    String username;
    String nickname;
    String password;
    String salt;
    String email;
    Integer type; //0-普通用户; 1-超级管理员; 2-版主;
    Integer status; //0-未激活; 1-已激活;
    String activationCode;
    Date createTime;
}
