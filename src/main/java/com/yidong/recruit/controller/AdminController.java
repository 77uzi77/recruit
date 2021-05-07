package com.yidong.recruit.controller;

import com.yidong.recruit.entity.ResultBean;
import com.yidong.recruit.entity.Sign;
import com.yidong.recruit.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class AdminController {

    @Autowired
    private UserService userService;

    @GetMapping("getNext/{direction}")
    @ApiOperation("排队结束，通知下一个")
    public ResultBean<String> getNext(@PathVariable String direction) {
        // 将用户的状态 更改为已完成
        String result = userService.getNext(direction);
//        System.out.println("接收前台getNext请求，处理下一个...");
//        log.info("接收前台getNext请求，处理下一个...");

        return new ResultBean<>(ResultBean.SUCCESS_CODE,result);
    }

    @PostMapping("findUserInfo")
    @ApiOperation("通过 条件查询 用户信息")
    public ResultBean<List<Sign>> findUserInfo(@RequestBody(required = false) Sign sign) {
        List<Sign> userInfo = userService.findUserInfo(sign);
        return new ResultBean<>(ResultBean.SUCCESS_CODE,userInfo);
    }

    @PutMapping("updateUserStatus/{openid}/{status}")
    @ApiOperation("更改 用户 状态")
    public ResultBean<String> updateUserStatus(@PathVariable String openid,@PathVariable String status) {
        userService.updateStatus(openid,status);
        return new ResultBean<>(ResultBean.SUCCESS_CODE,"更改成功！");
    }

    @GetMapping("getWaitQueue/{direction}")
    @ApiOperation("得到排队队列")
    public ResultBean<String[]> getWaitQueue(@PathVariable String direction) {

        String[] waitQueue = userService.getWaitQueue(direction);
        return new ResultBean<>(ResultBean.SUCCESS_CODE,waitQueue);
    }

}
