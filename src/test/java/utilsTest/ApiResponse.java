package utilsTest;

public record ApiResponse<T> (int status, T body, Object headers){}
