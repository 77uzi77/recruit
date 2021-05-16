package com.yidong.recruit.exception;

/**
 * @author lzc
 * @date 2021/4/25 14 13
 * discription
 */
public enum MyExceptionCodeEnum implements MyExceptionCode{
    // 已指明的异常,在异常使用时message并不返回前端，返回前端的为throw新的异常时指定的message
    SPECIFIED("-1","系统发生异常,请稍后重试"),

    // 常用业务异常
    USER_NAME_NULL("-1","用户名不能为空，请重新输入!"),
    USER_PASSWORD_NULL("-1","密码不能为空，请重新输入!"),
    USER_PASSWORD_WRONG("-1","密码错误,请检查后重新输入!"),
    PAGE_NUM_NULL("4001","页码不能为空"),
    PAGE_SIZE_NULL("4002","页数不能为空"),
    SEARCH_NULL("4004","搜索条件不能为空,请检查后重新输入!"),
    NO_LOGIN("3001", "用户未进行登录"),
    PARAM_NULL("500","传参为空"),

    //token的异常
    TOKEN_INCORRECT("-1","校验失败，返回登录"),

    // 报名信息格式异常
    NAME_INCORRECT("-1","请填写长度不超过10个的中文字符"),
    COLLEGE_INCORRECT("-1","请以中文填写学院名"),
    MAJOR_INCORRECT("-1","请以中文填写专业名"),
    PHONE_INCORRECT("-1","请填写合法手机号码"),
    SNO_INCORRECT("-1","请填写合法手机号码"),
    QQ_INCORRECT("-1","请正确填写qq号"),
    INTRODUCE_INCORRECT("-1","自我介绍控制在300字之内");


    private final String code;

    private final String message;

    /**
     * @Description : 构造自定义异常
     */
    MyExceptionCodeEnum(String code,String message){
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }


}
