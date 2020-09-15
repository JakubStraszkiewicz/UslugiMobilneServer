package api;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class UriUtils {
    public static URI uri(Class<?> clazz, String method, Object... vals) {
        return UriBuilder.fromResource(clazz)
                .path(clazz, method)
                .build(vals);
    }
}
