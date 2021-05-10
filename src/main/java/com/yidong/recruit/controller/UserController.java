package com.yidong.recruit.controller;

import com.yidong.recruit.entity.ResultBean;
import com.yidong.recruit.entity.Sign;
import com.yidong.recruit.service.UserService;
import com.yidong.recruit.util.AccessTokenUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author lzc
 * @date 2021/4/25 13 24
 * discription
 */
@RestController
@RequestMapping("user")
@Api(tags = "用户相关接口")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("getUserInfo/{code}")
    @ApiOperation("登录")
    public ResultBean<String> getUserInfo(@PathVariable String code) {

        String openid = AccessTokenUtil.getOpenid(code);
        log.info("登录成功，openid为:{}",openid);
//        System.out.println("openid：" + openid);
        return new ResultBean<>(ResultBean.SUCCESS_CODE,openid);
    }

    /**
     * @param sign
     * @return ResultBean<String>
     * @author lzc
     * @date 2021/4/25
     *  报名
     */
    @PostMapping("sign")
    @ApiOperation("报名")
    public ResultBean<String> sign(@RequestBody Sign sign) {
        userService.addOne(sign);
        return new ResultBean<>(ResultBean.SUCCESS_CODE,"报名成功！");
    }

    @GetMapping("getStatus/{openid}")
    @ApiOperation("得到用户状态")
    public ResultBean<String> getStatus(@PathVariable String openid) {
        String status = userService.getStatus(openid);
        return new ResultBean<>(ResultBean.SUCCESS_CODE,status);
    }

    @GetMapping("wait/{openid}")
    @ApiOperation("用户排队面试")
    public ResultBean<String> wait(@PathVariable String openid) {
        String message = userService.wait(openid);

        return new ResultBean<>(ResultBean.SUCCESS_CODE, message);
    }

    /**
     * @param openid
     * @return ResultBean<String>
     * @author ly
     * @date 2021/4/29
     *  查找是否重复报名
     */
    @GetMapping("ifHadSigned/{openid}")
    @ApiOperation("查看是否重复报名")
    public ResultBean<String> ifHadSigned(@PathVariable String openid) {
        String message = userService.ifHadSigned(openid);

        return new ResultBean<>(ResultBean.SUCCESS_CODE,message);
    }

    @GetMapping("getWaitQueueByOpenid/{openid}")
    @ApiOperation("根据openid查找等待队列")
    public ResultBean<String[]> getWaitQueueByOpenid(@PathVariable String openid) {
        String[] waitQueue = userService.getWaitQueueByOpenid(openid);
        return new ResultBean<>(ResultBean.SUCCESS_CODE,waitQueue);
    }

    @GetMapping("pushMessage")
    @ApiOperation("推送消息")
    // 通过排号编号 推送消息
    public ResultBean<String> pushMessage(/*@PathVariable*/ String openid) throws Exception {
        String message = userService.pushMessage(openid);

        return new ResultBean<>(ResultBean.SUCCESS_CODE,message);
    }
//
    @GetMapping("cancelWait/{openid}")
    @ApiOperation("取消排队")
    public ResultBean<String> cancelWait(@PathVariable String openid) {
        String message = userService.cancelWait(openid);
        return new ResultBean<>(ResultBean.SUCCESS_CODE,message);
    }

}
