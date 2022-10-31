package com.wh.wenniao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class Comment implements Serializable {
    @TableId(type= IdType.AUTO)
    Integer id;
    Integer userId;
    Integer entityType;
    Integer entityId;
    Integer targetId;
    String content;
    Integer status;
    Date createTime;
    String nickname;
    String voteId;
    Integer diceId;
}
