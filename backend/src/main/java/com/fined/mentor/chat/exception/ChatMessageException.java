package com.fined.mentor.chat.exception;

public class ChatMessageException extends RuntimeException {
    public ChatMessageException(String message) { super(message); }
    public ChatMessageException(String message, Throwable cause) { super(message, cause); }
}