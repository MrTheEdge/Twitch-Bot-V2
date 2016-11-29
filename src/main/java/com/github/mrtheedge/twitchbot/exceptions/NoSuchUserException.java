package com.github.mrtheedge.twitchbot.exceptions;

/**
 * Created by E.J. Schroeder on 11/24/2016.
 */
public class NoSuchUserException extends Exception {

    public NoSuchUserException(){}

    public NoSuchUserException(String message){
        super(message);
    }

}
