package com.yidong.recruit.service.impl;

import com.yidong.recruit.entity.Sign;
import com.yidong.recruit.mapper.UserMapper;
import com.yidong.recruit.service.UserService;
import com.yidong.recruit.util.TimeUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lzc
 * @date 2021/4/25 19 57
 * discription
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * @param one
     * @return void
     * @author lzc
     * @date 2021/4/25
     *  报名
     */
    @Override
    public void addOne(Sign one) {
        // 通过 openId 判断用户 是否 报名过
        Sign checkSign = new Sign();
        checkSign.setOpenid(one.getOpenid());
        checkSign.setId(null);
        Sign checkOne = userMapper.selectOne(checkSign);
        // 报名过则修改
        if (checkOne != null) {
            one.setId(checkOne.getId());
            userMapper.updateByPrimaryKeySelective(one);
        // 没报名则新增
        }else{
            userMapper.insertSelective(one);
        }
    }

    @Override
    public String getStatus(String openid) {
        // 通过 openId 得到用户状态
        Sign one = new Sign();
        one.setOpenid(openid);
        Sign res = userMapper.selectOne(one);
        if (res != null){
            // 已报名，则返回用户 状态
            return res.getStatus();
        }else{
            // 未报名，则返回默认值 0
            return "0";
        }
    }

    @Override
    public String wait(String openid) {
        String message;

        Sign one = new Sign();
        one.setOpenid(openid);
        Sign res = userMapper.selectOne(one);
        if (res != null){
            Map<String,String> data = new HashMap<>();
            data.put("openid",openid);
            data.put("time", TimeUtil.getCurrentTime());
            rabbitTemplate.convertAndSend("waitExchange","waitRouting",data);
            message = "排队成功！";
        }else{
            message = "排队失败！";
        }
        return message;
    }

    @Override
    public Sign getOne(String openid) {
        Sign one = new Sign();
        one.setOpenid(openid);
        return userMapper.selectOne(one);
    }

    @Override
    public void updateStatus(String openid,String status) {
        Sign one = new Sign();
        one.setOpenid(openid);
        one.setStatus(status);
        Example example = new Example(Sign.class);
        example.createCriteria().andEqualTo("openid",openid);
        userMapper.updateByExampleSelective(one,example);
    }

    @Override
    public String ifHadSigned(String openid) {
        // 通过 openId 判断用户 是否 报名过
        Sign checkSign = new Sign();
        checkSign.setOpenid(openid);
        String result;

        if (userMapper.selectOne(checkSign) != null){
            // 已报名则返回提示：您已报名，确定重复报名？
            result = "您已报名，确定重复报名？";
        }else{
            result = "是否确定报名？";
            // 未报名则新增: 调用addOne接口
        }
        return result;
    }

}
