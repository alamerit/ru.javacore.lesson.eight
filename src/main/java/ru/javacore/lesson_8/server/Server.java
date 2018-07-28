package ru.shafikov.javacore.server;

import ru.shafikov.javacore.ChatConstants;
import ru.shafikov.javacore.server.service.AuthService;
import ru.shafikov.javacore.server.service.BaseAuthService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Shafikov A. & help Evgenii Chentsov
 */
public final class Server {

    private final CopyOnWriteArrayList<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private final AuthService authService = new BaseAuthService();

    public void init(final int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            authService.start();
            Socket socket = null;
            //looking for the clients
            while (true) {
                System.out.println("Server is awaiting connections");
                socket = serverSocket.accept();
                System.out.println("A client has connected to the server");
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            authService.stop();
        }
    }

    public AuthService getAuthService() {
        return authService;
    }

    public synchronized boolean isNickNameBusy(final String nickName) {
        for (ClientHandler client : clients) {
            if (client.getNickName().equals(nickName)) return true;
        }
        return false;
    }

    public synchronized void sendMessageToClient(final String toNickName, final ClientHandler from,
                                                 final String message) {
        for (ClientHandler client : clients) {
            if (client.getNickName().equals(toNickName)) {
                from.sendMsg(from.getCurrentDate() + ChatConstants.PRIVATE_TO + client.getNickName() + ": " + message);
                client.sendMsg(from.getCurrentDate() + ChatConstants.PRIVATE_FROM + from.getNickName() + ": " + message);
                return;
            }
        }
        from.sendMsg(ChatConstants.NO_SUCH_USER_IN_ROOM);
    }

    public synchronized void broadcastMsg(final String message) {
        for (ClientHandler client : clients) {
            client.sendMsg(message);
        }
    }

    public synchronized void unsubscribe(final ClientHandler client) {
        clients.remove(client);
    }

    public synchronized void subscribe(final ClientHandler client) {
        clients.add(client);
    }

}
