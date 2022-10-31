package com.wh.wenniao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

@Data
public class Collect implements Serializable {
    @TableId(type = IdType.AUTO)
    Integer id;
    Integer userId;
    Integer postId;
}
