package com.yidong.recruit.service.impl;

import com.alibaba.fastjson.JSON;
import com.yidong.recruit.entity.Message;
import com.yidong.recruit.entity.Queue;
import com.yidong.recruit.entity.Sign;
import com.yidong.recruit.entity.TemplateData;
import com.yidong.recruit.listener.MessageConsumer;
import com.yidong.recruit.mapper.QueueMapper;
import com.yidong.recruit.mapper.UserMapper;
import com.yidong.recruit.service.UserService;
import com.yidong.recruit.util.AccessTokenUtil;
import com.yidong.recruit.util.RedisUtil;
import com.yidong.recruit.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.LockSupport;

import org.springframework.web.client.RestTemplate;

/**
 * @author lzc
 * @date 2021/4/25 19 57
 * discription
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private QueueMapper queueMapper;

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

        if (res != null) {

            // 将该用户的信息 加入 消息队列
            Map<String,String> data = new HashMap<>();
            data.put("openid",openid);
            data.put("time", TimeUtil.getCurrentTime());
            data.put("direction",res.getDirection());

            // 判断排队用户的方向 是后台还是前端
            if("后台".equals(res.getDirection())) {
//                Integer backstageCount = (Integer) redisUtil.get("backstageCount");
//                // 通过 backstageCount 更新 用户 需要等待的人数
//                if (backstageCount != null){
//                    redisUtil.set(openid,backstageCount);
//                    redisUtil.incr("backstageCount",1);
//                }else {
//                    redisUtil.set(openid,0);
//                    redisUtil.set("backstageCount",1);
//                }

                // 将用户 保存 到 后台等待队列
                String backstageQueue = (String) redisUtil.get("backstageQueue");
                String user = JSON.toJSONString(res);
                log.info("报名信息转化成的json对象：{}",user);
                if(backstageQueue != null) {
                    redisUtil.set("backstageQueue",backstageQueue + ";" + user);
                }else{
                    redisUtil.set("backstageQueue",user);
                }

                // 分发到 后台等待队列
                rabbitTemplate.convertAndSend("waitExchange","backstageRouting",data);

            }else {
                // 通过 foreCount 更新 用户 需要等待的人数
//                Integer backstageCount = (Integer) redisUtil.get("foreCount");
//                if (backstageCount != null){
//                    redisUtil.set(openid,backstageCount);
//                    redisUtil.incr("foreCount",1);
//                }else {
//                    redisUtil.set(openid,0);
//                    redisUtil.set("foreCount",1);
//                }

                // 将用户 保存 到 前端等待队列
                String foreQueue = (String) redisUtil.get("foreQueue");
                String user = JSON.toJSONString(res);
                log.info("报名信息转化成的json对象：{}",user);
                if(foreQueue != null) {
                    redisUtil.set("foreQueue",foreQueue + ";" + user);
                }else{
                    redisUtil.set("foreQueue",user);
                }

                // 分发到 前端等待队列
                rabbitTemplate.convertAndSend("waitExchange","foreRouting",data);
            }

            message = "排队成功！";
        }else{
            message = "排队失败！";
        }
        return message;
    }

    /***
     * @param openid
     * @return Sign
     * @author lzc
     * @date 2021/5/6
     *  通过openid查询用户
     */
    @Override
    public Sign getOne(String openid) {
        Sign one = new Sign();
        one.setOpenid(openid);
        return userMapper.selectOne(one);
    }

    /***
     * @param openid
     * @param status
     * @return void
     * @author lzc
     * @date 2021/5/6
     *  更新用户状态
     */
    @Override
    public void updateStatus(String openid,String status) {
        Sign one = new Sign();
        one.setStatus(status);
        one.setOpenid(openid);
//        System.out.println(one.getOpenid());
        Example example = new Example(Sign.class);
        example.createCriteria().andEqualTo("openid",openid);
        userMapper.updateByExampleSelective(one,example);
        log.info("service成功修改用户{}状态为2",openid);
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
     * @param direction
     * @return String
     * @author lzc
     * @date 2021/5/7
     * 得到等待队列
     */
    @Override
    public String[] getWaitQueue(String direction) {
        String waitQueue;
        if ("fore".equals(direction) || "前端".equals(direction)) {
            waitQueue = (String) redisUtil.get("foreQueue");
        } else {
            waitQueue = (String) redisUtil.get("backstageQueue");
        }

        if (waitQueue != null){
            return waitQueue.split(";");
        }

        return null;
    }

    /**
     * @param
     * @return String
     * @author lzc
     * @date 2021/5/6
     *  得到前端队列的第一个用户
     */
//    @Override
//    public String getFirstForeUser() {
//        return (String) redisUtil.get("firstForeUser");
//    }

    /**
     * @param
     * @return String
     * @author lzc
     * @date 2021/5/6
     *  得到后台队列的第一个用户
     */
//    @Override
//    public String getFirstBackstageUser() {
//        return (String) redisUtil.get("firstBackstageUser");
//    }

    @Override
    public String getNext(String direction) {
        if ("fore".equals(direction) || "前端".equals(direction)) {
//            MessageConsumer.isForeFinish = true;
            LockSupport.unpark(MessageConsumer.isForeFinish);
            log.info("前端用户面试结束... 开始下一个");
        } else {
//            MessageConsumer.isBackstageFinish = true;
            LockSupport.unpark(MessageConsumer.isBackstageFinish);
            log.info("后台用户面试结束... 开始下一个");
        }
        return "处理成功！";
    }

    /**
     * @param openid
     * @return String
     * @author lzc
     * @date 2021/5/7
     * 根据openid 查找 等待队列
     */
    @Override
    public String[] getWaitQueueByOpenid(String openid) {
        Sign sign = new Sign();
        sign.setOpenid(openid);
        Sign user = userMapper.selectOne(sign);

        String direction = user.getDirection();
        return getWaitQueue(direction);
    }


    /**
     * @param sign
     * @return Example
     * @author lzc
     * @date 2021/4/30
     *  构造 条件查询 的 example
     */
    public Example createExample(Sign sign) {
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

    @Override
    public String pushMessage(Integer id) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        String access_token = AccessTokenUtil.getAccessToken();
        String url = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + access_token;

        // 通过id查找对应排队的人的openid
        Queue queue = new Queue();
        queue.setId(id);
        Queue resQueue = queueMapper.selectOne(queue);
        String openid = resQueue.getOpenid();
        System.out.println(openid);

        // 封装推送消息的模板内容
        Map<String, TemplateData> data = new HashMap<>();
        data.put("面试通知",new TemplateData("您可以面试啦"));
        data.put("面试地点",new TemplateData("教五创客C区"));

        // 拼接推送的模板
        Message message = new Message();
        message.setId(id);
        message.setTouser(openid);
        message.setTemplate_id("KvBGv6vFbfxUvryDC1XQlpyHVzz3E5V8Q1Z0D86u47Q");
        //    message.setPage("/pages/index");
        message.setData(data);

        // 发送
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url,message,String.class);
        System.out.println("推送返回的信息是：" + responseEntity.getBody());
        return responseEntity.getBody();
    }

    @Override
    public String cancelWait(String openid) {
        // 判断用户是否在 队列中的 计数器

        boolean flag = false;
        String result;

        Sign sign = new Sign();
        sign.setOpenid(openid);
        Sign user = userMapper.selectOne(sign);
        String direction = user.getDirection();

        String[] waitQueue = getWaitQueue(direction);

        StringBuilder newQueue = new StringBuilder();
        for (int i = 0; i < waitQueue.length; i++) {
            if (waitQueue[i].contains(openid)) {
                flag = true;
                if (i == waitQueue.length - 1) {
                    waitQueue[i] = waitQueue[i].substring(0,waitQueue[i].length() - 1) + ",\"state\":\"-1\"}";
                } else {
                    waitQueue[i] = waitQueue[i].substring(0,waitQueue[i].length() - 1) + ",\"state\":\"-1\"};";
                }
            }
            newQueue.append(waitQueue[i]);
        }

        if (flag) {
            if ("前端".equals(direction)) {
                redisUtil.set("foreQueue",newQueue.toString());
            } else {
                redisUtil.set("backstageQueue",newQueue.toString());
            }
            result = "取消排队成功！";
        } else {
            result = "取消排队失败！";
        }

        return result;
    }

}
