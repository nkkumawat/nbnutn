package me.nkkumawat.chatzzz.Model;

import java.io.Serializable;

/**
 * Created by sonu on 20/1/18.
 */

public class Contacts implements Serializable {
    public String name;
    public String number;

    public Contacts(String name , String number) {
        this.name = name;
        this.number = number;
    }
}