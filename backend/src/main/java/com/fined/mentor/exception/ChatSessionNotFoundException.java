package com.fined.mentor.exception;

public class ChatSessionNotFoundException extends RuntimeException {
    public ChatSessionNotFoundException(String message) {
        super(message);
    }
}
