package ru.practicum.shareit.item.exceptions;

public class CommentNotAllowedException extends RuntimeException {

    public CommentNotAllowedException(String message) {
        super(message);
    }
}
