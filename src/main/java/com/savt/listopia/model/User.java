package com.savt.listopia.model;

public class User {
    private String name;

    public User(String uuid) { name = uuid; }

    @Override
    public String toString() {
        return "User{"
            + "name='" + name + '\'' + '}';
    }
}
