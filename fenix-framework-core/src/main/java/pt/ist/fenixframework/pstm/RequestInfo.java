package pt.ist.fenixframework.pstm;

public class RequestInfo {
    private static final ThreadLocal<String> REQUEST_URI = new ThreadLocal<String>() {
        @Override
        protected String initialValue() {
            return "";
        }
    };

    public static String getRequestURI() {
        return REQUEST_URI.get();
    }

    public static void setRequestURI(String uri) {
        REQUEST_URI.set(uri);
    }

    public static void clear() {
        REQUEST_URI.remove();
    }
}
