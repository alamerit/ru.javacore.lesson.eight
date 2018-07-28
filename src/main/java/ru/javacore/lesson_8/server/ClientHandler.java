package ru.shafikov.javacore.server;

import ru.shafikov.javacore.ChatConstants;
import ru.shafikov.javacore.server.service.BaseAuthService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * @author Shafikov A. & help Evgenii Chentsov
 */
public final class ClientHandler {

    private Server server;
    private Socket socket;
    private String nickName;
    private DataInputStream in;
    private DataOutputStream out;

    public ClientHandler(final Server server, final Socket socket) {
        this.socket = socket;
        this.server = server;
        this.nickName = "";


        try {
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            final Thread messageHandlerThread = new Thread(this::handleMessages);
            messageHandlerThread.start();
        } catch (IOException e) {
            throw new RuntimeException ("Issues during account creation occurred" );
        }
    }

    public String getNickName() {
        return nickName;
    }

    private void handleMessages() {
        try {
            handleClientRequest();
            getMessages();
        }  catch (SocketException | EOFException e) {
            System.out.println("Session has been aborted");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            server.unsubscribe(this);
            server.broadcastMsg(nickName + " has exited from the chat");
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleClientRequest() throws IOException {
        while (true) {
            final String message = in.readUTF();
            if (message.startsWith(ChatConstants.AUTHORIZE_FLAG)) {
                if (authorizeClient(message)) break;
            } else if (message.startsWith(ChatConstants.REGISTER_FLAG)) {
                if (registerClient(message)) break;
            }
        }
    }

    private boolean authorizeClient(final String message) {
        final int expectedTokensAmount = 3;
        final String[] authData = message.split("\\s");
        if (authData.length != expectedTokensAmount) {
            sendMsg("Please fill all the fields (no spaces)");
            return false;
        }

        final String nickName = server.getAuthService().getNickByLoginPass(authData[1], authData[2]);
        if (nickName != null) {
            if (!server.isNickNameBusy(nickName)) {
                sendMsg(ChatConstants.AUTHORIZE_OK_FLAG + nickName);
                this.nickName = nickName;
                server.broadcastMsg(this.nickName + ChatConstants.CHAT_ENTERED_FLAG);
                server.subscribe(this);
                return true;
            } else sendMsg("Account is already in use");
        } else sendMsg("Wrong login/password pair");

        return false;
    }

    private boolean registerClient(final String message) {
        final int expectedTokensAmount = 4;
        final String[] authData = message.split("\\s");
        if (authData.length != expectedTokensAmount) {
            sendMsg("Please fill all the fields (no spaces)");
            return false;
        }

        final String nickName = authData[3];
        final String login = authData[1];
        final String password = authData[2];
        if (server.getAuthService() instanceof BaseAuthService) {
            final BaseAuthService authService = ((BaseAuthService) server.getAuthService());
            final boolean containsLogin = authService.containsLogin(login);
            final boolean containsNickName = authService.containsNickName(nickName);
            if (!containsLogin) {
                if (!containsNickName) {
                    authService.addEntry(login, password, nickName);
                    sendMsg(ChatConstants.REGISTER_OK_FLAG + nickName);
                    this.nickName = nickName;
                    server.broadcastMsg(this.nickName + ChatConstants.CHAT_ENTERED_FLAG);
                    server.subscribe(this);
                    return true;
                } else sendMsg("Nickname is already in use");
            } else sendMsg("Login is already in use");
        }

        return false;
    }

    public String getCurrentDate() {
        final SimpleDateFormat messageDateFormat = new SimpleDateFormat("HH:mm");
        return  "|" + messageDateFormat.format(new Date()) + "| ";
    }

    private void getMessages() throws IOException{
        while (true) {
            final String messageText = in.readUTF();
            System.out.println(ChatConstants.FROM + nickName + ": " + messageText);
            if (ChatConstants.TERMINATE_CONNECTION_FLAG.equals(messageText)) break;

            if (messageText.startsWith(ChatConstants.PRIVATE_MESSAGE_FLAG)) {
                final String[] parts = messageText.split("\\s");
                final String nickName = parts[1];
                final String message = messageText.substring(ChatConstants.PRIVATE_MESSAGE_FLAG.length()
                        + nickName.length() + 1);
                server.sendMessageToClient(nickName, this, message);
            } else server.broadcastMsg(getCurrentDate() + nickName + ": " + messageText);
        }
    }

    public void sendMsg(final String message) {
        try {
            out.writeUTF(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
