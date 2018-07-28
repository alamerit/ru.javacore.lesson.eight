package ru.shafikov.javacore.server.service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Evgenii Chentsov
 */
public final class BaseAuthService implements AuthService {

    private List<Entry> entries;

    public BaseAuthService() {
        entries = new ArrayList<>();
    }

    @Override
    public void start() {}  //do nothing

    @Override
    public void stop() {}   //do nothing

    @Override
    public String getNickByLoginPass(final String login, final String password) {
        for (Entry currentEntry : entries) {
            if (currentEntry.login.equals(login) && currentEntry.password.equals(password)) {
                return currentEntry.nickName;
            }
        }
        return null;
    }

    public void addEntry(final String login, final String password, final String nickName) {
        entries.add(new Entry(login, password, nickName));
    }

    public boolean containsNickName(final String nickName) {
        if (nickName == null) return false;
        for (Entry account : entries) {
            if (nickName.equals(account.nickName)) return true;
        }
        return false;
    }

    public boolean containsLogin(final String login) {
        if (login == null) return false;
        for (Entry account : entries) {
            if (login.equals(account.login)) return true;
        }
        return false;
    }

    private class Entry {
        private String login;
        private String password;
        private String nickName;

        public Entry(final String login, final String password, final String nickName) {
            this.login = login;
            this.password = password;
            this.nickName = nickName;
        }

    }

}
