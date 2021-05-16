package com.yidong.recruit.entity;

import com.yidong.recruit.exception.MyException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


/**
 * @author lzc
 * discription 返回前端的 统一返回结果
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Api("统一返回结果")
public class ResultBean<T> implements Serializable {

    // 处理成功的状态码
    public static final String SUCCESS_CODE = "1";
    // 发生未知错误的状态码
    public static final String UNSPECIFIED_CODE = "500";
    public static final String INCCORECT_CODE = "-1";


    @ApiModelProperty("提示信息")
    private String message = "success";
    @ApiModelProperty("响应码")
    private String code = ResultBean.SUCCESS_CODE;
    @ApiModelProperty("返回数据")
    private T data;

    public ResultBean(String code, T data) {
        this.code = code;
        this.data = data;
    }

    /**
     * @Description : 此时系统发生未知异常
     */
    public ResultBean(Throwable e) {
        super();
        this.message = "发生未知错误，请稍后重试!";
        this.code = ResultBean.UNSPECIFIED_CODE;
    }

    public ResultBean(MyException e) {
        super();
        this.message = e.getMessage();
        this.code = e.getCode();
    }
}