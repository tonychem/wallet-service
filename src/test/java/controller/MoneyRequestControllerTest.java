package controller;

//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import configuration.TestConfiguration;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Disabled;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mockito;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//import ru.tonychem.application.ApplicationController;
//import ru.tonychem.config.JwtTokenFilter;
//import ru.tonychem.domain.dto.MoneyTransferRequest;
//import ru.tonychem.domain.dto.MoneyTransferResponse;
//import ru.tonychem.exception.GlobalExceptionHandler;
//import ru.tonychem.exception.model.UnauthorizedOperationException;
//import ru.tonychem.in.controller.MoneyRequestController;
//import ru.tonychem.in.dto.PlayerRequestMoneyDto;
//import ru.tonychem.in.dto.TransactionsListDto;
//import ru.tonychem.util.JwtUtils;
//
//import java.math.BigDecimal;
//import java.util.Collection;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//
//import static org.hamcrest.CoreMatchers.is;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@ContextConfiguration(classes = TestConfiguration.class)
//@ExtendWith(SpringExtension.class)
//@Disabled
public class MoneyRequestControllerTest {
//    private MockMvc mvc;
//    private ApplicationController mockApplicationController;
//    private static ObjectMapper objectMapper = new ObjectMapper();
//
//    private String validJwtToken;
//    private String invalidJwtToken;
//
//    @BeforeEach
//    public void init() {
//        mockApplicationController = Mockito.mock(ApplicationController.class);
//        mvc = MockMvcBuilders
//                .standaloneSetup(new MoneyRequestController(mockApplicationController))
//                .setControllerAdvice(new GlobalExceptionHandler())
//                .addFilters(new JwtTokenFilter(objectMapper))
//                .build();
//
//        validJwtToken = generateValidTestToken();
//        invalidJwtToken = "invalid.jwt.token";
//    }
//
//    @DisplayName("Should return collection of pending money requests when token is valid")
//    @Test
//    public void shouldReturnCollectionOfPendingMoneyRequestsWhenTokenIsValid() throws Exception {
//        MoneyTransferRequest moneyTransferRequest = new MoneyTransferRequest(UUID.randomUUID(),
//                "sender", "recipient", BigDecimal.ONE);
//        Collection<MoneyTransferRequest> expectedCollectionResponse = List.of(moneyTransferRequest);
//
//        when(mockApplicationController.getPendingMoneyRequests(any(), any()))
//                .thenReturn(expectedCollectionResponse);
//
//        mvc.perform(get("/player-management/money-request")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .header("Authorization", "Bearer " + validJwtToken))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.length()", is(1)))
//                .andExpect(jsonPath("$[0].id", is(moneyTransferRequest.getId().toString())))
//                .andExpect(jsonPath("$[0].moneyFrom", is(moneyTransferRequest.getMoneyFrom())))
//                .andExpect(jsonPath("$[0].moneyTo", is(moneyTransferRequest.getMoneyTo())));
//
//        verify(mockApplicationController).getPendingMoneyRequests(any(), any());
//    }
//
//    @DisplayName("Should post money request when token is valid")
//    @Test
//    public void shouldPostMoneyRequestTokenIsValid() throws Exception {
//        PlayerRequestMoneyDto playerRequestMoneyDto = new PlayerRequestMoneyDto("donor", 1.0);
//
//        when(mockApplicationController.requestMoneyFrom(any(), any(), any(), any(), any()))
//                .thenReturn(true);
//
//        mvc.perform(post("/player-management/money-request")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsBytes(playerRequestMoneyDto))
//                        .header("Authorization", "Bearer " + validJwtToken))
//                .andExpect(status().isOk());
//
//        verify(mockApplicationController).requestMoneyFrom(any(), any(), any(), any(), any());
//    }
//
//    @DisplayName("Should approve pending money requests when some of the offered ids are invalid")
//    @Test
//    public void shouldApprovePendingMoneyRequestsWhenSomeIdsAreInvalid() throws Exception {
//        List<String> listOfSuppliedTransactionIdsWhereSomeAreFailing = List.of(UUID.randomUUID().toString(),
//                UUID.randomUUID().toString(), "failing-uuid");
//
//        TransactionsListDto transactionsListDto = new TransactionsListDto(listOfSuppliedTransactionIdsWhereSomeAreFailing);
//
//        when(mockApplicationController.approvePendingRequest(any(), any(), any()))
//                .thenReturn(new MoneyTransferResponse(null, null));
//
//        mvc.perform(post("/player-management/money-request/approve")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsBytes(transactionsListDto))
//                        .header("Authorization", "Bearer " + validJwtToken))
//                .andExpect(status().isOk());
//
//        verify(mockApplicationController, times(listOfSuppliedTransactionIdsWhereSomeAreFailing.size() - 1))
//                .approvePendingRequest(any(), any(), any());
//    }
//
//    @DisplayName("Should decline pending money requests when some of the offered ids are invalid")
//    @Test
//    public void shouldDeclinePendingMoneyRequestsWhenSomeIdsAreInvalid() throws Exception {
//        List<String> listOfSuppliedTransactionIdsWhereSomeAreFailing = List.of(UUID.randomUUID().toString(),
//                UUID.randomUUID().toString(), "failing-uuid");
//        TransactionsListDto transactionsListDto = new TransactionsListDto(listOfSuppliedTransactionIdsWhereSomeAreFailing);
//
//        doNothing().when(mockApplicationController).declinePendingRequest(any(), any(), any());
//
//        mvc.perform(post("/player-management/money-request/decline")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsBytes(transactionsListDto))
//                        .header("Authorization", "Bearer " + validJwtToken))
//                .andExpect(status().isOk());
//
//        verify(mockApplicationController, times(listOfSuppliedTransactionIdsWhereSomeAreFailing.size() - 1))
//                .declinePendingRequest(any(), any(), any());
//    }
//
//    @DisplayName("Should throw forbidden when fetching pending money requests when token is invalid")
//    @Test
//    public void shouldThrowForbiddenWhenFetchingPendingMoneyRequestsWhenTokenIsInvalid() throws Exception {
//        mvc.perform(get("/player-management/money-request")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .header("Authorization", "Bearer " + invalidJwtToken))
//                .andExpect(status().isForbidden());
//
//        verify(mockApplicationController, never()).getPendingMoneyRequests(any(), any());
//    }
//
//    @DisplayName("Should throw forbidden when requesting money when token is invalid")
//    @Test
//    public void shouldThrowForbiddenWhenRequestingMoneyWhenTokenIsInvalid() throws Exception {
//        mvc.perform(post("/player-management/money-request")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .header("Authorization", "Bearer " + invalidJwtToken))
//                .andExpect(status().isForbidden());
//
//        verify(mockApplicationController, never()).requestMoneyFrom(any(), any(), any(), any(), any());
//    }
//
//    @DisplayName("Should throw forbidden when accepting money request when token is invalid")
//    @Test
//    public void shouldThrowForbiddenWhenAcceptingMoneyRequestWhenTokenIsInvalid() throws Exception {
//        mvc.perform(post("/player-management/money-request/accept")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .header("Authorization", "Bearer " + invalidJwtToken))
//                .andExpect(status().isForbidden());
//
//        verify(mockApplicationController, never()).approvePendingRequest(any(), any(), any());
//    }
//
//    @DisplayName("Should throw forbidden when declining money request when token is invalid")
//    @Test
//    public void shouldThrowForbiddenWhenDecliningMoneyRequestWhenTokenIsInvalid() throws Exception {
//        mvc.perform(post("/player-management/money-request/decline")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .header("Authorization", "Bearer " + invalidJwtToken))
//                .andExpect(status().isForbidden());
//
//        verify(mockApplicationController, never()).declinePendingRequest(any(), any(), any());
//    }
//
//    private String generateValidTestToken() {
//        Map<String, Object> claims = Map.of("id", 1L,
//                "login", "admin",
//                "session-id", UUID.randomUUID());
//
//        return JwtUtils.generateToken(claims);
//    }
}
