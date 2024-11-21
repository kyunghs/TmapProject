package com.myapplication.models;
public class Place {
    private String name;

    // 생성자
    public Place(String name) {
        this.name = name;
    }

    // getter
    public String getName() {
        return name;
    }

    // setter (필요하면 사용)
    public void setName(String name) {
        this.name = name;
    }
}
