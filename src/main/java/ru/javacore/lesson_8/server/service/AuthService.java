package ru.shafikov.javacore.server.service;

/**
 * @author Evgenii Chentsov
 */
public interface AuthService {

    void start();

    void stop();

    String getNickByLoginPass(String login, String password);

}
