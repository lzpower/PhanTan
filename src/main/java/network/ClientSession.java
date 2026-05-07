package network;

public final class ClientSession {

    private static volatile boolean remoteMode = true;
    private static volatile String host = "Dell-6N85";
    private static volatile int port = 9090;
    private static volatile String clientName = defaultClientName();

    private ClientSession() {
    }

    public static void configureRemote(String hostValue, int portValue, String clientNameValue) {
        remoteMode = true;
        host = hostValue == null || hostValue.isBlank() ? host : hostValue.trim();
        port = portValue > 0 ? portValue : port;
        clientName = clientNameValue == null || clientNameValue.isBlank() ? defaultClientName() : clientNameValue.trim();
    }

    public static void configureLocal() {
        remoteMode = false;
    }

    public static boolean isRemoteMode() {
        return remoteMode;
    }

    public static String getHost() {
        return host;
    }

    public static int getPort() {
        return port;
    }

    public static String getClientName() {
        return clientName;
    }

    public static String defaultClientName() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (Exception ex) {
            return "client-" + System.getProperty("user.name", "unknown");
        }
    }
}