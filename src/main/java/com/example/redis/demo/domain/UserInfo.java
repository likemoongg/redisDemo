package com.example.redis.demo.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@ToString
@Getter
@Setter
public class UserInfo implements Serializable {

    private static final long serialVersionUID = 4075321806604869898L;

    private String userName;

    private Integer userAge;

    private Long userId;

}
