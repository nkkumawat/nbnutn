package me.nkkumawat.chatzzz.Socket;

import android.util.Log;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.emitter.Emitter;

/**
 * Created by sonu on 21/1/18.
 */

public class SocketConnection {
    public static io.socket.client.Socket socket = null;
    public static void ConnectSocket() {
        try {
            socket = IO.socket("http://192.168.1.70:3000");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        socket.on(io.socket.client.Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d("Socket" , "Emmited in SocketConnection");
            }
        });
        socket.connect();
    }
}
