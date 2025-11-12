package com.fined.mentor.exception;

public class ChatSessionException extends RuntimeException {
    public ChatSessionException(String message) { super(message); }
    public ChatSessionException(String message, Throwable cause) { super(message, cause); }
}
