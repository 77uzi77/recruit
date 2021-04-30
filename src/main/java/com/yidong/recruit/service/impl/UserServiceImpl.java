package com.yidong.recruit.service.impl;

import com.yidong.recruit.entity.Sign;
import com.yidong.recruit.mapper.UserMapper;
import com.yidong.recruit.service.UserService;
import com.yidong.recruit.util.RedisUtil;
import com.yidong.recruit.util.TimeUtil;
import io.swagger.models.auth.In;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import javax.naming.ldap.Rdn;
import java.util.HashMap;
import java.util.List;
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

    @Autowired
    private RedisUtil redisUtil;

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
        Sign checkOne = userMapper.selectOne(checkSign);
        // 更新状态
        one.setStatus("1");
        // 报名过则修改
        if (checkOne != null) {
            one.setId(checkOne.getId());
            userMapper.updateByPrimaryKeySelective(one);
        // 没报名则新增
        }else{
            userMapper.insertSelective(one);
        }
    }


    /**
     * @param openid
     * @return String
     * @author lzc
     * @date 2021/4/30
     *  通过 openid 得到 用户状态
     */
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

    /**
     * @param openid
     * @return String
     * @author lzc
     * @date 2021/4/30
     *  用户 排队 功能
     */
    @Override
    public String wait(String openid) {

        String message;
        Sign one = new Sign();
        one.setOpenid(openid);
        // 通过openid判断是否已报名
        Sign res = userMapper.selectOne(one);
        if (res != null){

            // 判断排队用户的方向 是后台还是前端
            if("后台".equals(res.getDirection())) {
                Integer backstageCount = (Integer) redisUtil.get("backstageCount");
                // 通过 backstageCount 更新 用户 需要等待的人数
                if (backstageCount != null){
                    redisUtil.set(openid,backstageCount);
                    redisUtil.incr("backstageCount",1);
                }else {
                    redisUtil.set(openid,0);
                    redisUtil.set("backstageCount",1);
                }
            }else {
                // 通过 foreCount 更新 用户 需要等待的人数
                Integer backstageCount = (Integer) redisUtil.get("foreCount");
                if (backstageCount != null){
                    redisUtil.set(openid,backstageCount);
                    redisUtil.incr("foreCount",1);
                }else {
                    redisUtil.set(openid,0);
                    redisUtil.set("foreCount",1);
                }
            }

            Map<String,String> data = new HashMap<>();
            data.put("openid",openid);
            data.put("time", TimeUtil.getCurrentTime());
            data.put("direction",res.getDirection());
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

    /**
     * @param sign
     * @return List<Sign>
     * @author lzc
     * @date 2021/4/30
     *  条件查询 用户信息
     */
    @Override
    public List<Sign> findUserInfo(Sign sign) {
        //构建查询条件
        Example example = createExample(sign);
        //根据构建的条件查询数据
        return userMapper.selectByExample(example);
    }

    /**
     * @param sign
     * @return Example
     * @author lzc
     * @date 2021/4/30
     *  构造 条件查询 的 example
     */
    public Example createExample(Sign sign){
        Example example = new Example(Sign.class);
        Example.Criteria criteria = example.createCriteria();
        if(sign != null){
            // 方向
            if (StringUtils.hasText(sign.getDirection())){
                criteria.andEqualTo("direction",sign.getDirection());
            }
            // 状态
            if (StringUtils.hasText(sign.getStatus())){
                criteria.andEqualTo("status",sign.getStatus());
            }
        }
        return example;
    }

}
