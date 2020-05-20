package cn.edu.cdtu.drive.util;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author HarrisonLee
 * @date 2020/5/17 15:52
 */
@Setter
@Getter
@ToString
public class Result implements Serializable {

    private static final long serialVersionUID = -5372450875750675775L;

    private HttpStatus status;
    private Map<String, Object> map;

    private Result( HttpStatus status) {
        this.status = status;
        this.map = new HashMap<>();
    }

    public static Result result() {
        return new Result(HttpStatus.OK);
    }

    public static Result result(HttpStatus status) {
        return new Result(status);
    }

    public Result setStatus(HttpStatus status) {
        this.status = status;
        return this;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public Result put(String key, Object object) {
        this.map.put(key, object);
        return this;
    }

    public Result putAll(Map<String, Object>m) {
        this.map.putAll(m);
        return this;
    }
}
