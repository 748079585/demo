package com.example.demo.bean;

import lombok.Data;

import java.io.Serializable;

@Data
public class ApiResult implements Serializable {

    private static final long serialVersionUID = 5616477943476838995L;

    private static final String SUCCESS = "success";
    private static final String FAIL = "failure";

    /**
     * 返回码，200 正常
     */
    private int code = 200;

    /**
     * 返回信息
     */
    private String msg = "成功";

    /**
     * 返回数据
     */
    private Object data;

    /**
     * 系统当前时间
     */
    private Long timestamp = System.currentTimeMillis();

    /**
     * 获取成功状态结果
     *
     */
    public static ApiResult success() {
        return success(null);
    }

    /**
     * 获取成功状态结果
     *
     */
    public static ApiResult success_msg(String msg) {
        ApiResult apiResult = new ApiResult();
        apiResult.setCode(200);
        apiResult.setMsg(msg);
        return apiResult;
    }

    /**
     * 获取成功状态结果
     *
     * @param data 返回数据
     */
    public static ApiResult success(Object data) {
        ApiResult apiResult = new ApiResult();
        apiResult.setCode(200);
        apiResult.setMsg(SUCCESS);
        apiResult.setData(data);
        return apiResult;
    }

    /**
     * 获取失败状态结果
     */
    public static ApiResult failure() {
        return failure(-1, FAIL, null);
    }

    /**
     * 获取失败状态结果
     *
     * @param msg (自定义)失败消息
     * @return
     */
    public static ApiResult failure(String msg) {
        return failure(400, msg, null);
    }



    /**
     * 获取失败返回结果
     *
     * @param code 错误码
     * @param msg  错误信息
     * @param data 返回结果
     * @return
     */
    public static ApiResult failure(int code, String msg, Object data) {
        ApiResult result = new ApiResult();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(data);
        if (data instanceof String) {
            String m = (String) data;
            if (!m.matches("^.*error$")) {
                m += "error";
            }
        }
        return result;
    }

}
