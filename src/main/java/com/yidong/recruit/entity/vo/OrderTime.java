package com.yidong.recruit.entity.vo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lzc
 * @date 2021/5/16 13 11
 * discription
 */
@Data
@Api("用户预约时间")
public class OrderTime {

    @ApiModelProperty("用户标识id")
    private String openid;

    @ApiModelProperty("开始面试时间")
    private String startTime;

    @ApiModelProperty("结束面试时间")
    private String endTime;

}
