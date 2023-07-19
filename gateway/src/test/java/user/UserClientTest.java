package user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import ru.practicum.shareit.user.UserClient;
import ru.practicum.shareit.user.dto.UserRequestDto;

@ExtendWith(MockitoExtension.class)
public class UserClientTest {
    @Mock
    private RestTemplateBuilder restTemplate;

    private UserClient userClient = new UserClient("localhost", restTemplate);
    private UserRequestDto userRequestDto;

    @BeforeEach
    public void init() {
        userRequestDto = UserRequestDto.builder()
                .name("name")
                .email("email@email.com")
                .build();
    }

/*    @Test
    public void addUser_Normal() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), (ParameterizedTypeReference<UserRequestDto>) any())).thenReturn(
                new ResponseEntity<>(new UserRequestDto(), HttpStatus.OK));

        ResponseEntity<Object> result = userClient.addUser(userRequestDto);

        assertTrue(result.hasBody());
        assertEquals(HttpStatus.OK, result.getStatusCode());
    }*/
}
