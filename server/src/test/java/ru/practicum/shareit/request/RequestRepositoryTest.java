package ru.practicum.shareit.request;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
public class RequestRepositoryTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RequestRepository requestRepository;

    private User savedUser1;
    private User savedUser2;
    private Request savedRequest1;
    private Request savedRequest2;
    private Request savedRequest3;
    private int from = 0;
    private int size = 10;
    private PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);

    @BeforeEach
    public void beforeEach() {
        User user1 = User.builder()
                .name("name")
                .email("email@email.com")
                .build();
        User user2 = User.builder()
                .name("other name")
                .email("otheremail@email.com")
                .build();
        savedUser1 = userRepository.save(user1);
        savedUser2 = userRepository.save(user2);

        Request request1 = Request.builder()
                .description("description")
                .requester(savedUser1)
                .created(Timestamp.valueOf(LocalDateTime.now()))
                .build();
        savedRequest1 = requestRepository.save(request1);

        Request request2 = Request.builder()
                .description("description 2")
                .requester(savedUser1)
                .created(Timestamp.valueOf(LocalDateTime.now()))
                .build();
        savedRequest2 = requestRepository.save(request2);

        Request request3 = Request.builder()
                .description("description 3")
                .requester(savedUser2)
                .created(Timestamp.valueOf(LocalDateTime.now()))
                .build();
        savedRequest3 = requestRepository.save(request3);
    }

    @AfterEach
    public void afterEach() {
        userRepository.deleteAll();
        requestRepository.deleteAll();
    }

    @Test
    public void findRequestByRequesterId_Normal() {
        List<Request> requests = requestRepository.findAllByRequesterId(savedUser1.getId(), page).getContent();

        assertEquals(2, requests.size());
        assertEquals(savedRequest1, requests.get(0));
    }

    @Test
    public void findRequestByRequesterId_NoRequests() {
        List<Request> requests = requestRepository.findAllByRequesterId(savedUser1.getId() + 999, page).getContent();

        assertEquals(0, requests.size());
    }

    @Test
    public void findAllOrderByCreated_Normal() {
        List<Request> requests = requestRepository.findAllOrderByCreated(savedUser1.getId(), page).getContent();

        assertEquals(1, requests.size());
        assertEquals(savedRequest3, requests.get(0));
    }
}