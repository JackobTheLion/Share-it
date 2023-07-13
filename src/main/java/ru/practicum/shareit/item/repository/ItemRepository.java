package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.item.model.Item;

public interface ItemRepository extends JpaRepository<Item, Long> {
    Page<Item> findAllByOwnerId(Long ownerId, Pageable page);

    Page<Item> findItemByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndIsAvailableIsTrue(String text,
                                                                                                       String text2,
                                                                                                       Pageable page);
}
