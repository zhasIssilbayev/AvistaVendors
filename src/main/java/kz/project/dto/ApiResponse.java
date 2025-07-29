package kz.project.dto;

public class ApiResponse<T> {
    private T data;
    private String message;

    public ApiResponse(T data) {
        this.data = data;
        this.message = "OK";
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
