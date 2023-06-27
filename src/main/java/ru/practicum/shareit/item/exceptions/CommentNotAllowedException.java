package ru.practicum.shareit.item.exceptions;

public class CommentNotAllowedException extends RuntimeException {
    public CommentNotAllowedException() {
    }

    public CommentNotAllowedException(String message) {
        super(message);
    }

    public CommentNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommentNotAllowedException(Throwable cause) {
        super(cause);
    }
}
