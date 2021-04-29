package com.yidong.recruit.service.impl;

import com.yidong.recruit.entity.Sign;
import com.yidong.recruit.mapper.UserMapper;
import com.yidong.recruit.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author lzc
 * @date 2021/4/25 19 57
 * discription
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

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
    public boolean ifHadSigned(Sign sign) {
        // 通过 openId 判断用户 是否 报名过
        Sign checksign = new Sign();
        checksign.setOpenid(checksign.getOpenid());
        Sign returnSign = new Sign();
        returnSign = userMapper.selectOne(checksign);
        if (returnSign != null){
            // 已报名则返回提示：您已报名，确定重复报名？
            return true;
        }else{

            // 未报名则新增: 调用addOne接口
            return false;
        }
    }


}
