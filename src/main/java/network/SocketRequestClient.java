package network;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public final class SocketRequestClient {

    private SocketRequestClient() {
    }

    public static Response send(Request request) {
        try (Socket socket = new Socket(ClientSession.getHost(), ClientSession.getPort());
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            out.writeObject(request);
            out.flush();
            return (Response) in.readObject();
        } catch (Exception ex) {
            throw new RuntimeException("Không thể gửi request socket: " + ex.getMessage(), ex);
        }
    }
}