package com.yidong.recruit.controller;

import com.yidong.recruit.entity.ResultBean;
import com.yidong.recruit.entity.Sign;
import com.yidong.recruit.entity.vo.OrderTime;
import com.yidong.recruit.service.UserService;
import com.yidong.recruit.util.AccessTokenUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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
    public ResultBean/*<String>*/<Map<String,String>> sign(@RequestBody Sign sign) {
        Map<String,String> checkMap = userService.addOne(sign);
        Map<String,String> map = new HashMap<>();
        map.put("data","报名成功！");

     //   userService.addOne(sign);
     //   return new ResultBean<>(ResultBean.SUCCESS_CODE,"报名成功！");
        if(checkMap.isEmpty()) return new ResultBean<>(ResultBean.SUCCESS_CODE,map);
        else return new ResultBean<>(ResultBean.INCCORECT_CODE,checkMap);
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

        return new ResultBean<>(ResultBean.  SUCCESS_CODE, message);
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

    @PostMapping("orderTime")
    @ApiOperation("预约面试")
    public ResultBean<String> orderTime(@RequestBody OrderTime time) {
        String message = userService.orderTime(time);
        return new ResultBean<>(ResultBean.SUCCESS_CODE,message);
    }

    @GetMapping("getTime/{openid}")
    @ApiOperation("获得用户预约时间")
    public ResultBean<String> getTime(@PathVariable String openid) {
        String time = userService.getTime(openid);
        return new ResultBean<>(ResultBean.SUCCESS_CODE,time);
    }

    @GetMapping("getOrderCount/{date}/{openid}")
    @ApiOperation("获得该日期已预约人数")
    public ResultBean<Integer> getOrderCount(@PathVariable String date,@PathVariable String openid) {
        Integer count = userService.getOrderCount(date,openid);
        return new ResultBean<>(ResultBean.SUCCESS_CODE,count);
    }

}
