package com.yidong.recruit.entity;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;

/**
 * @author lzc
 * @date 2021/4/25 19 37
 * discription 报名表
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Api("报名表")
public class Sign {

    @Id
    @KeySql(useGeneratedKeys = true)
    @ApiModelProperty("id")
    private Integer id;

    @ApiModelProperty("用户标识id")
    private String openid;

    @ApiModelProperty("学号")
    private String sno;

    @ApiModelProperty("姓名")
    private String name;

    @ApiModelProperty("qq")
    private String qq;

    @ApiModelProperty("电话号码")
    private String phoneNum;

    @ApiModelProperty("学院")
    private String college;

    @ApiModelProperty("专业")
    private String major;

    @ApiModelProperty("方向")
    private String direction;

    @ApiModelProperty("自我介绍")
    private String introduce;

    @ApiModelProperty("状态")
    private String status;
}
