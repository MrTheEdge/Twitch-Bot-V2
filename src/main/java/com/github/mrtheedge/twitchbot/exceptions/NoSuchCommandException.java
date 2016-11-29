package com.github.mrtheedge.twitchbot.exceptions;

/**
 * Created by E.J. Schroeder on 11/25/2016.
 */
public class NoSuchCommandException extends Exception {

    public NoSuchCommandException(){}

    public NoSuchCommandException(String message){
        super(message);
    }

}
