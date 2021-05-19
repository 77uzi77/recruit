package com.yidong.recruit.service.impl;

import com.alibaba.fastjson.JSON;
import com.yidong.recruit.entity.Message;
import com.yidong.recruit.entity.Sign;
import com.yidong.recruit.entity.TemplateData;
import com.yidong.recruit.entity.vo.OrderTime;
import com.yidong.recruit.listener.MessageConsumer;
import com.yidong.recruit.mapper.UserMapper;
import com.yidong.recruit.service.UserService;
import com.yidong.recruit.util.AccessTokenUtil;
import com.yidong.recruit.util.RedisUtil;
import com.yidong.recruit.util.TimeUtil;
import io.swagger.models.auth.In;
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
import java.util.regex.Pattern;

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
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * @param one
     * @return void
     * @author lzc
     * @date 2021/4/25
     * 报名
     */
    @Override
    public Map<String, String> addOne(Sign one) {

        // 检查报名信息填写格式
        String pattern1 = "[\u4e00-\u9fa5]+";    // 限制只能填写中文

        Map<String,String> checkMap = new HashMap();
        // true：表示报名格式正确
        //boolean flag = true;

        if (!Pattern.matches(pattern1,one.getName()) || one.getName().getBytes().length > 30){
             checkMap.put("nameError","请填写长度不超过10个的中文字符");
            /*flag = false;
            throw new MyException("请填写长度不超过10个的中文字符");*/
        }
        if (!Pattern.matches(pattern1,one.getCollege()) || one.getCollege().getBytes().length > 30){
            checkMap.put("collegeError","请以中文填写学院名");
            /*flag = false;
            throw new MyException("请以中文填写学院名");*/
        }
        if (!Pattern.matches(pattern1,one.getMajor()) || one.getMajor().getBytes().length > 30){
            checkMap.put("majorError","请以中文填写专业名");
            /*flag = false;
            throw new MyException("请以中文填写专业名");*/
        }

      //  String patternPhone = "[1][3578]\\d{9}";
        String patternPhone = "^((13[0-9])|(14[5-9])|(15([0-3]|[5-9]))|(16[6-7])|(17[1-8])|(18[0-9])|(19[1|3])|(19[5|6])|(19[8|9]))\\d{8}$";
        if (!Pattern.matches(patternPhone,one.getPhoneNum()) ){
            checkMap.put("phoneError","请填写合法手机号码");
           /* flag = false;
            throw new MyException("请填写合法手机号码");*/
        }

        String patternSno = "[3][12][2][0][0-9]{6}";
        if (!Pattern.matches(patternSno,one.getSno()) ){
            checkMap.put("snoError","请正确填写学号");
            /*flag = false;
            throw new MyException("请正确填写学号");*/
        }

        String patternQQ = "[1-9][0-9]{4,14}";
        if (!Pattern.matches(patternQQ,one.getQq()) ){
             checkMap.put("qqError","请正确填写qq号");
            /*flag = false;
            throw new MyException("请正确填写qq号");*/
        }

        if(one.getIntroduce().getBytes().length > 900){
            checkMap.put("introduceError","自我介绍控制在300字之内");
           /* flag = false;
            throw new MyException("自我介绍控制在300字之内");*/
        }


        if(checkMap.isEmpty() /*flag*/) {
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
             //   checkMap.put("signResult", "重报成功！");
                // 没报名则新增
            } else {
                userMapper.insertSelective(one);
             //   checkMap.put("signResult", "报名成功！");
            }
        }
        return  checkMap;
    }


    /**
     * @param openid
     * @return String
     * @author lzc
     * @date 2021/4/30
     * 通过 openid 得到 用户状态
     */
    @Override
    public String getStatus(String openid) {
        // 通过 openId 得到用户状态
        Sign one = new Sign();
        one.setOpenid(openid);
        Sign res = userMapper.selectOne(one);
        if (res != null) {
            // 已报名，则返回用户 状态
            return res.getStatus();
        } else {
            // 未报名，则返回默认值 0
            return "0";
        }
    }

    /**
     * @param openid
     * @return String
     * @author lzc
     * @date 2021/4/30
     * 用户 排队 功能
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
            Map<String, String> data = new HashMap<>();
            data.put("openid", openid);
            data.put("time", TimeUtil.getCurrentTime());
            data.put("direction", res.getDirection());

            // 判断排队用户的方向 是后台还是前端
            if ("后台".equals(res.getDirection())) {
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
                log.info("报名信息转化成的json对象：{}", user);
                if (backstageQueue != null) {
                    redisUtil.set("backstageQueue", backstageQueue + ";" + user);
                } else {
                    redisUtil.set("backstageQueue", user);
                }

                // 分发到 后台等待队列
                rabbitTemplate.convertAndSend("waitExchange", "backstageRouting", data);

            } else {
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
                log.info("报名信息转化成的json对象：{}", user);
                if (foreQueue != null) {
                    redisUtil.set("foreQueue", foreQueue + ";" + user);
                } else {
                    redisUtil.set("foreQueue", user);
                }

                // 分发到 前端等待队列
                rabbitTemplate.convertAndSend("waitExchange", "foreRouting", data);
            }

            message = "排队成功！";
        } else {
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
    public void updateStatus(String openid, String status) {
        Sign one = new Sign();
        one.setStatus(status);
        one.setOpenid(openid);
//        System.out.println(one.getOpenid());
        Example example = new Example(Sign.class);
        example.createCriteria().andEqualTo("openid", openid);
        userMapper.updateByExampleSelective(one, example);
        log.info("service成功修改用户{}状态为2", openid);
    }

    /**
     * @param openid
     * @return String
     * @author ly
     * @date 2021/5/16
     *  判断用户是否已报名
     */
    @Override
    public String ifHadSigned(String openid) {
        // 通过 openId 判断用户 是否 报名过
        Sign checkSign = new Sign();
        checkSign.setOpenid(openid);
        String result;

        if (userMapper.selectOne(checkSign) != null) {
            // 已报名则返回提示：您已报名，确定重复报名？
            result = "您已报名，确定重复报名？";
        } else {
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
     * 条件查询 用户信息
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

        if (waitQueue != null) {
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
     * 得到后台队列的第一个用户
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
     * 根据openid 查找  等待队列
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
     * 构造 条件查询 的 example
     */
    public Example createExample(Sign sign) {
        Example example = new Example(Sign.class);
        Example.Criteria criteria = example.createCriteria();
        if (sign != null) {
            // openid
            if (StringUtils.hasText(sign.getOpenid())) {
                criteria.andEqualTo("openid",sign.getOpenid());
            }
            // 方向
            if (StringUtils.hasText(sign.getDirection())) {
                criteria.andEqualTo("direction", sign.getDirection());
            }
            // 状态
            if (StringUtils.hasText(sign.getStatus())) {
                criteria.andEqualTo("status", sign.getStatus());
            }
        }
        return example;
    }

    /**
     * @param openid
     * @return String
     * @author ly
     * @date 2021/5/16
     * 推送消息
     */
    @Override
    public String pushMessage(String openid) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        String access_token = AccessTokenUtil.getAccessToken();
        String url = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + access_token;

    /*    // 通过id查找对应排队的人的openid
        Queue queue = new Queue();
        queue.setId(id);
        Queue resQueue = queueMapper.selectOne(queue);
        String openid = resQueue.getOpenid(); */
       // System.out.println(openid);

        // 通过openid查找 排队的人的姓名、方向
        Sign sign = new Sign();
        sign.setOpenid(openid);
        Sign resSign = userMapper.selectOne(sign);

        // 封装推送消息的模板内容
        Map<String, TemplateData> data = new HashMap<>();
        if (resSign != null) {
            data.put("thing1", new TemplateData(resSign.getName()));
            data.put("thing2", new TemplateData("教五创客C区"));
            data.put("thing3", new TemplateData(resSign.getDirection()));
        }

        // 拼接推送的模板
        Message message = new Message();
        //     message.setId(id);
        message.setTouser(openid);
        message.setTemplate_id("1ae24siVl2_2qLkLnA-N_p-F7P_pbvp9YL4fGG1RPmw");
        //    message.setPage("/pages/index");
        message.setData(data);


        // 发送
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, message, String.class);
        System.out.println("推送返回的信息是：" + responseEntity.getBody());
        return responseEntity.getBody();
    }

    /**
     * @param time
     * @return String
     * @author lzc
     * @date 2021/5/16
     * 预约面试时间
     */
    @Override
    public String orderTime(OrderTime time) {

        // 通过openid获取用户方向
        Sign user = new Sign();
        user.setOpenid(time.getOpenid());
//        Sign one = userMapper.selectOne(user);

        // 如果用户已预约，则将 其 之前预约的日期 的预约人数 减一
//        if (StringUtils.hasText(one.getDate())) {
//            redisUtil.incr(one.getDate() + ":" + one.getDirection(), -1);
//        }
        // 增加 该日期 该方向 预约的人数
//        String key = time.getDate() + ":" + one.getDirection();
//        if (redisUtil.get(key) != null) {
//            redisUtil.incr(key,1);
//        } else {
//            redisUtil.set(key,1);
//        }

        // 保存用户 预约 日期
        user.setDate(time.getDate());
        Example example = new Example(Sign.class);
        example.createCriteria().andEqualTo("openid", time.getOpenid());
        userMapper.updateByExampleSelective(user, example);

        return "预约成功！";
    }

    /**
     * @param openid
     * @return String
     * @author lzc
     * @date 2021/5/16
     *  得到面试时间
     */
    @Override
    public String getTime(String openid) {
        Sign one = new Sign();
        one.setOpenid(openid);

        Sign sign = userMapper.selectOne(one);

        return sign.getDate();
    }

    @Override
    public Integer getOrderCount(String date, String openid) {
        // 通过openid获取用户方向
        Sign user = new Sign();
        user.setOpenid(openid);
        Sign one = userMapper.selectOne(user);

        Sign count = new Sign();
        count.setDirection(one.getDirection());
        count.setDate(date);

        // 根据 日期 + 方向 查找 已预约人数
//        String key = date + ":" + one.getDirection();
//        if (redisUtil.get(key) != null) {
//            return redisUtil.get(key) + "";
//        } else {
//            return "0";
//        }

        return userMapper.selectCount(count);
    }


    /**
     * @param openid
     * @return String
     * @author lzc
     * @date 2021/5/16
     *  取消排队
     */
    @Override
    public String cancelWait(String openid) {
        // 判断用户是否已排队的 标识
        boolean flag = false;
        String result;

        // 通过openid 查找用户的 方向
        Sign sign = new Sign();
        sign.setOpenid(openid);
        Sign user = userMapper.selectOne(sign);
        String direction = user.getDirection();

        // 根据 用户方向 得到 等待队列
        String[] waitQueue = getWaitQueue(direction);

        if (waitQueue == null) {
            result = "取消排队失败！";
        } else {
            // 如果 用户 在等待 队列 中，则拼接 取消排队标识
            StringBuilder newQueue = new StringBuilder();
            for (int i = 0; i < waitQueue.length; i++) {
                if (waitQueue[i].contains(openid)) {
                    flag = true;
                    if (i == waitQueue.length - 1) {
                        waitQueue[i] = waitQueue[i].substring(0, waitQueue[i].length() - 1) + ",\"state\":\"-1\"}";
                    } else {
                        waitQueue[i] = waitQueue[i].substring(0, waitQueue[i].length() - 1) + ",\"state\":\"-1\"};";
                    }
                } else {
                    if (i != waitQueue.length - 1) {
                        waitQueue[i] = waitQueue[i] + ";";
                    }
                }
                newQueue.append(waitQueue[i]);
            }

            if (flag) {
                if ("前端".equals(direction)) {
                    redisUtil.set("foreQueue", newQueue.toString());
                } else {
                    redisUtil.set("backstageQueue", newQueue.toString());
                }
                result = "取消排队成功！";
            } else {
                result = "取消排队失败！";
            }
        }

        return result;
    }

}
