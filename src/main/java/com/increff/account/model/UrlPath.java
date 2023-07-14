package com.increff.account.model;

/**
 * @author gautham
 */
public enum UrlPath {
    UI("/ui"),
    PROFILE("/ui/profile"),
    USERS("/ui/users");

    private String name;

    UrlPath(String name) {
        this.name = name;
    }

    public String getVal() {
        return this.name;
    }

}
