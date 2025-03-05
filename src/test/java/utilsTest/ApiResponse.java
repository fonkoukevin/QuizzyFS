package utilsTest;

import java.util.Map;

public record ApiResponse<T> (int status, T body, Map<String, String> headers){
    public String getLocationId() {
        if (headers == null) return null;
        var loc = headers.get("Location").split("/");
        return loc[loc.length - 1];
    }
}
