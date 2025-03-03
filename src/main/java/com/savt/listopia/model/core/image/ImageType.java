package com.savt.listopia.model.core.image;

public enum ImageType {
    BACKDROP(1),
    POSTER(2),
    LOGO(3),
    PROFILES(4);

    private final int id;

    ImageType(int id){
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
