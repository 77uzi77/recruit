package com.yidong.recruit.controller;

import com.yidong.recruit.entity.ResultBean;
import com.yidong.recruit.entity.Sign;
import com.yidong.recruit.listener.RabbitConsumer;
import com.yidong.recruit.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author lzc
 * @date 2021/4/29 11 49
 * discription
 */
@RestController()
@RequestMapping("admin")
@Api("管理员相关接口")
public class AdminController {

    @GetMapping("getNext")
    @ApiOperation("排队结束，通知下一个")
    public ResultBean<String> getNext(){
        // 将用户的状态 更改为已完成
        RabbitConsumer.isFinish = true;
        System.out.println("接收前台getNext请求，处理下一个...");

        return new ResultBean<>(ResultBean.SUCCESS_CODE,"处理成功");
    }

}
