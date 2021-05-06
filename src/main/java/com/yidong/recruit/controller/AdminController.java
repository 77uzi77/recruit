package com.yidong.recruit.controller;

import com.yidong.recruit.entity.ResultBean;
import com.yidong.recruit.entity.Sign;

import com.yidong.recruit.listener.MessageConsumer;
import com.yidong.recruit.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * @author lzc
 * @date 2021/4/29 11 49
 * discription
 */
@RestController()
@RequestMapping("admin")
@Api(tags = "管理员相关接口")
public class AdminController {

    @Autowired
    private UserService userService;

    @GetMapping("getNext/{direction}")
    @ApiOperation("排队结束，通知下一个")
    public ResultBean<String> getNext(@PathVariable String direction){
        // 将用户的状态 更改为已完成
//        RabbitConsumer.isFinish = true;
        if ("fore".equals(direction)){
            MessageConsumer.isForeFinish = true;
        }else{
            MessageConsumer.isBackstageFinish = true;
        }

        System.out.println("接收前台getNext请求，处理下一个...");

        return new ResultBean<>(ResultBean.SUCCESS_CODE,"处理成功");
    }

    @PostMapping("findUserInfo")
    @ApiOperation("通过 条件查询 用户信息")
    public ResultBean<List<Sign>> findUserInfo(@RequestBody(required = false) Sign sign){
        List<Sign> userInfo = userService.findUserInfo(sign);
        return new ResultBean<>(ResultBean.SUCCESS_CODE,userInfo);
    }

    @PutMapping("updateUserStatus/{openid}/{status}")
    @ApiOperation("更改 用户 状态")
    public ResultBean<String> updateUserStatus(@PathVariable String openid,@PathVariable String status){
        userService.updateStatus(openid,status);
        return new ResultBean<>(ResultBean.SUCCESS_CODE,"更改成功！");
    }

    @GetMapping("getWaitQueue")
    @ApiOperation("得到排队队列")
    public ResultBean<String> getWaitQueue(){
        String waitQueue = userService.getWaitQueue();
        return new ResultBean<>(ResultBean.SUCCESS_CODE,waitQueue);
    }

}
