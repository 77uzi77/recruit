package com.yidong.recruit.entity;

import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Api("推送消息模板")
public class Message {
    private Integer id;
    private String touser;
    private String template_id;
  //  private String page;
    private Map<String,TemplateData> data;  // 推送文字
}
