package cn.edu.cdtu.drive.aop;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author HarrisonLee
 * @date 2020/4/5 1:16
 */

@NoArgsConstructor
@Setter
@Getter
public class WebLog {
    /**
     * 操作描述
     */
    private String description;

    /**
     * 开始
     */
    private String startTime;

    /**
     * 消耗时间
     */
    private Long spendTime;

    /**
     * URI
     */
    private String uri;

    /**
     * 请求类型
     */
    private String method;

    /**
     * IP地址
     */
    private String ip;

    /**
     * 请求参数
     */
    private Object parameter;


    private String endTime;

    private Object result;

    @Override
    public String toString() {
        return "WebLog{" +
                "api描述='" + description + '\'' +
                ", 开始时间='" + startTime + '\'' +
                ", 结束时间='" + endTime + '\'' +
                ", 消耗时间=" + spendTime + "毫秒" +
                ", uri='" + uri + '\'' +
                ", HTTP方法='" + method + '\'' +
                ", ip='" + ip + '\'' +
                ", 方法参数=" + parameter +
                ", 返回值=" + result +
                '}';
    }
}
