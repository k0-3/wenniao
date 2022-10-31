package com.wh.wenniao.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class LoginTicket implements Serializable {
    private int id;
    private int userId;
    private String ticket;
    private int status;
    private Date expired;
}
