package com.yidong.recruit.exception;

/**
 * @author lzc
 * @date 2021/4/25 14 10
 * discription
 */
public class MyException extends RuntimeException {
    private static final long serialVersionUID = -7864604160297181941L;

    private final String code;

    /**
     * @Description : 指定枚举类中的错误类
     */
    public MyException(final MyExceptionCode exceptionCode) {
        super(exceptionCode.getMessage());
        this.code = exceptionCode.getCode();
    }

    /**
     * @Description : 指定具体业务错误的信息
     */
    public MyException(final String message) {
        super(message);
        this.code = MyExceptionCodeEnum.SPECIFIED.getCode();
    }

    public String getCode() {
        return code;
    }
}
