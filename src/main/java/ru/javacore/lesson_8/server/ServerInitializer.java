package ru.shafikov.javacore.server;

/**
 * @author Shafikov A. & help Evgenii Chentsov
 */
public final class ServerInitializer {

    public static void main(String[] args) {
        final Server chatServer = new Server();
        chatServer.init(8189);
    }

}
