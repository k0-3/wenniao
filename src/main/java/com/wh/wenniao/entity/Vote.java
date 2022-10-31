package com.wh.wenniao.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;


@Data
public class Vote implements Serializable {
    String voteTitle;
    Date expire;
    Integer userCount;
    LinkedHashMap<String,Integer> op;
}
