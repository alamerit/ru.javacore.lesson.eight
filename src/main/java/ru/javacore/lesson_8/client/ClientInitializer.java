package ru.shafikov.javacore.client;

/**
 * @author Shafikov A. & help Evgenii Chentsov
 */
public final class ClientInitializer {

    public static void main(String[] args) {
        ClientWindow client = new ClientWindow();
        client.init("localhost", 8189);
    }

}
