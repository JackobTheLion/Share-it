package ru.practicum.shareit.request.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.request.model.Request;

public interface RequestRepository extends JpaRepository<Request, Long> {
    Page<Request> findAllByRequesterId(Long userId, Pageable page);

    @Query(value = "select * from item_requests as r where requester_id <> ?1 order by r.created", nativeQuery = true)
    Page<Request> findAllOrderByCreated(Long userId, Pageable page);
}
