package am.trade.api.controller;

import am.trade.api.service.TradeJournalService;
import com.am.security.context.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TradeJournalController.class)
@ContextConfiguration(classes = TradeJournalController.class)
@AutoConfigureMockMvc(addFilters = false)
class TradeJournalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TradeJournalService tradeJournalService;

    @BeforeEach
    void setUp() {
        UserContext.setUserId("test-user-from-token");
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void getJournalEntriesByUser_withoutUserContext_returnsUnauthorized() throws Exception {
        UserContext.clear();
        mockMvc.perform(get("/v1/journal/user"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getJournalEntriesByUser_withUserContext_usesJournalUserPath() throws Exception {
        when(tradeJournalService.getJournalEntriesByUser(eq("test-user-from-token"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/v1/journal/user"))
                .andExpect(status().isOk());
    }
}
