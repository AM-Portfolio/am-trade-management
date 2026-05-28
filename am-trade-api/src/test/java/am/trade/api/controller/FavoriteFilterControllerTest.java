package am.trade.api.controller;

import am.trade.api.dto.FavoriteFilterResponse;
import am.trade.api.service.FavoriteFilterService;
import am.trade.api.service.UserTradeService;
import com.am.security.context.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FavoriteFilterController.class)
@ContextConfiguration(classes = FavoriteFilterController.class)
@AutoConfigureMockMvc(addFilters = false)
class FavoriteFilterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FavoriteFilterService favoriteFilterService;

    @MockBean
    private UserTradeService userTradeService;

    @BeforeEach
    void setUp() {
        UserContext.setUserId("test-user-from-token");
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void getUserFilters_withoutUserContext_returnsUnauthorized() throws Exception {
        UserContext.clear();
        mockMvc.perform(get("/v1/filters"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUserFilters_withUserContext_succeedsWithoutUserIdParam() throws Exception {
        when(favoriteFilterService.getUserFilters(eq("test-user-from-token")))
                .thenReturn(Collections.<FavoriteFilterResponse>emptyList());

        mockMvc.perform(get("/v1/filters"))
                .andExpect(status().isOk());
    }
}
