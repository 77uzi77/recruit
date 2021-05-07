package com.yidong.recruit.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Queue {
    private Integer id;
    private String openid;
    private Integer num;
    private String type;
}
