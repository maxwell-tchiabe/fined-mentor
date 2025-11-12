package com.fined.mentor.exception;

public class ChatMessageNotFoundException extends RuntimeException {
    public ChatMessageNotFoundException(String message) {
        super(message);
    }
}
