package com.yidong.recruit.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用来封装订阅消息
 */
@Data
@NoArgsConstructor
public class TemplateData {
    private  String value;
    public TemplateData(String value){
        this.value = value;
    }
}
