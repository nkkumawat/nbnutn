package me.nkkumawat.chatzzz.Model;

import java.io.Serializable;

/**
 * Created by sonu on 20/1/18.
 */

public class Contacts implements Serializable {
    public String name;
    public String number;
    public String pictureUrl;

    public Contacts(String name , String number, String pictureUrl) {
        this.name = name;
        this.number = number;
        this.pictureUrl = pictureUrl;
    }
}