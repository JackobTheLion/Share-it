package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.item.dto.ItemInRequestDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.exceptions.RequestNotFoundException;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.request.service.RequestService;
import ru.practicum.shareit.user.exceptions.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RequestServiceTest {
    @Mock
    private RequestRepository requestRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RequestService requestService;

    private ItemRequestRequestDto itemRequestRequestDtoToSave;
    private ItemRequestResponseDto savedItemRequestRequestDto;
    private Request savedRequest;
    private Long requestId = 1L;
    private Item requestItem;
    private ItemInRequestDto requestItemDto;
    private List<Item> requestItems;
    private List<ItemInRequestDto> requestItemsDto;
    private User requester;
    private Long userId = 1L;
    private Timestamp now = Timestamp.valueOf(LocalDateTime.now());
    private List<Request> requests;
    private int from = 0;
    private int size = 10;
    private PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);

    @BeforeEach
    public void beforeEach() {
        requester = User.builder()
                .id(userId)
                .build();

        itemRequestRequestDtoToSave = new ItemRequestRequestDto();
        itemRequestRequestDtoToSave.setDescription("description");
        itemRequestRequestDtoToSave.setRequesterId(requester.getId());

        savedRequest = Request.builder()
                .id(requestId)
                .description(itemRequestRequestDtoToSave.getDescription())
                .created(now)
                .requester(requester)
                .items(requestItems)
                .build();

        savedItemRequestRequestDto = ItemRequestResponseDto.builder()
                .id(savedRequest.getId())
                .description(savedRequest.getDescription())
                .created(savedRequest.getCreated().toLocalDateTime())
                .items(requestItemsDto)
                .build();

        requests = new ArrayList<>();
        requests.add(savedRequest);

        requestItem = Item.builder()
                .id(1L)
                .name("name")
                .description("description")
                .ownerId(1L)
                .isAvailable(true)
                .request(savedRequest)
                .build();
        requestItems = new ArrayList<>();
        requestItems.add(requestItem);

        requestItemDto = ItemInRequestDto.builder()
                .id(requestItem.getId())
                .name(requestItem.getName())
                .description(requestItem.getDescription())
                .available(requestItem.getIsAvailable())
                .requestId(requestId)
                .build();
        requestItemsDto = new ArrayList<>();
        requestItemsDto.add(requestItemDto);
    }

    @Test
    public void addRequest_Normal() {
        when(userRepository.findById(requester.getId())).thenReturn(Optional.of(requester));
        when(requestRepository.save(any(Request.class))).thenReturn(savedRequest);

        ItemRequestResponseDto actualRequest = requestService.addRequest(itemRequestRequestDtoToSave);

        assertEquals(savedItemRequestRequestDto, actualRequest);
        verify(requestRepository).save(any(Request.class));
    }

    @Test
    public void addRequest_NoSuchUser() {
        when(userRepository.findById(requester.getId())).thenReturn(Optional.empty());

        Throwable e = assertThrows(UserNotFoundException.class, () -> requestService.addRequest(itemRequestRequestDtoToSave));

        assertEquals(String.format("User id %s not found.", itemRequestRequestDtoToSave.getRequesterId()), e.getMessage());
        verify(requestRepository, never()).save(any(Request.class));
    }

    @Test
    public void findRequest_Normal() {
        when(userRepository.findById(requester.getId())).thenReturn(Optional.of(requester));
        when(requestRepository.findById(savedRequest.getId())).thenReturn(Optional.of(savedRequest));

        ItemRequestResponseDto actualRequest = requestService.findRequest(savedRequest.getId(), userId);

        assertEquals(savedItemRequestRequestDto, actualRequest);
    }

    @Test
    public void findRequest_NoSuchRequest() {
        when(userRepository.findById(requester.getId())).thenReturn(Optional.of(requester));
        when(requestRepository.findById(savedRequest.getId())).thenReturn(Optional.empty());

        Throwable e = assertThrows(RequestNotFoundException.class, ()
                -> requestService.findRequest(savedRequest.getId(), userId));

        assertEquals(String.format("Request id %s not found.", savedRequest.getId()), e.getMessage());
    }

    @Test
    public void findUserRequest_Normal() {
        when(userRepository.findById(requester.getId())).thenReturn(Optional.of(requester));
        when(requestRepository.findAllByRequesterId(any(), any())).thenReturn(new PageImpl<>(requests));

        List<ItemRequestResponseDto> result = requestService.findUserRequest(userId, from, size);

        assertEquals(1, result.size());
        assertEquals(savedItemRequestRequestDto, result.get(0));
    }

    @Test
    public void findUserRequest_NoSuchUser() {
        when(userRepository.findById(requester.getId())).thenReturn(Optional.empty());

        Throwable e = assertThrows(UserNotFoundException.class, () ->
                requestService.findUserRequest(userId, from, size));

        assertEquals(String.format("User id %s not found.", userId), e.getMessage());
        verify(requestRepository, never()).findAllByRequesterId(anyLong(), any(PageRequest.class));
    }

    @Test
    public void findUserRequest_Empty() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(requester));
        when(requestRepository.findAllByRequesterId(anyLong(), any(PageRequest.class))).thenReturn(new PageImpl<>(new ArrayList<>()));

        List<ItemRequestResponseDto> result = requestService.findUserRequest(userId, from, size);

        assertTrue(result.isEmpty());
        verify(requestRepository, times(1)).findAllByRequesterId(anyLong(), any(PageRequest.class));
    }

    @Test
    public void findAllRequests_Normal() {
        when(requestRepository.findAllOrderByCreated(any(), any())).thenReturn(new PageImpl<>(requests));

        List<ItemRequestResponseDto> result = requestService.findAllRequests(userId, from, size);

        assertEquals(1, result.size());
        assertEquals(savedItemRequestRequestDto, result.get(0));
    }

    @Test
    public void findAllRequests_Empty() {
        when(requestRepository.findAllOrderByCreated(any(), any())).thenReturn(new PageImpl<>(new ArrayList<>()));

        List<ItemRequestResponseDto> result = requestService.findAllRequests(userId, from, size);

        assertTrue(result.isEmpty());
    }
}