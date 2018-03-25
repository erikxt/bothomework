package bot.entity;

/**
 * 应答对象 给前端解析用
 */
public class Response {

    // 应答状态码  0表示成功
    private Integer code;

    // 应答对象
    private Object data;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    private Response(Integer code, Object data) {
        this.code = code;
        this.data = data;
    }

    public static Response getSuccessResp(Object data) {
        return new Response(0, data);
    }

    public static Response getErrorResp(Object data) {
        return new Response(-1, data);
    }
}
