package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;


public class CommentMapper {
    public static CommentDto mapToDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor().getName())
                .created(comment.getCreated().toLocalDateTime())
                .build();
    }

    public static Comment mapFromDto(CommentDto commentDto, Long userId, Long itemId) {
        User user = new User();
        user.setId(userId);

        Item item = new Item();
        item.setId(itemId);

        return Comment.builder()
                .author(user)
                .item(item)
                .text(commentDto.getText())
                .build();
    }
}