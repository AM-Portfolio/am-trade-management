package am.trade.kafka.consumer;

import am.trade.models.kafka.PortfolioUpdateEventMirror;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.support.Acknowledgment;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class PortfolioUpdateConsumerServiceTest {

    private PortfolioUpdateConsumerService consumerService;
    
    @Mock
    private ObjectMapper objectMapper;
    
    @Mock
    private Acknowledgment acknowledgment;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        consumerService = new PortfolioUpdateConsumerService(objectMapper);
    }

    @Test
    public void testConsume_Success() throws Exception {
        // Arrange
        String jsonMessage = "{\"userId\":\"U123\",\"totalValue\":10000.0}";
        PortfolioUpdateEventMirror mockEvent = PortfolioUpdateEventMirror.builder()
                .userId("U123")
                .totalValue(10000.0)
                .build();
                
        when(objectMapper.readValue(eq(jsonMessage), eq(PortfolioUpdateEventMirror.class)))
                .thenReturn(mockEvent);

        // Act
        consumerService.consume(jsonMessage, acknowledgment);

        // Assert
        verify(objectMapper, times(1)).readValue(eq(jsonMessage), eq(PortfolioUpdateEventMirror.class));
        verify(acknowledgment, times(1)).acknowledge(); // Ensure message is acked on success
    }

    @Test
    public void testConsume_ExceptionDoesNotAck() throws Exception {
        // Arrange
        String jsonMessage = "{\"invalid\":\"json\"}";
        when(objectMapper.readValue(eq(jsonMessage), eq(PortfolioUpdateEventMirror.class)))
                .thenThrow(new RuntimeException("Parsing error"));

        // Act
        consumerService.consume(jsonMessage, acknowledgment);

        // Assert
        verify(objectMapper, times(1)).readValue(eq(jsonMessage), eq(PortfolioUpdateEventMirror.class));
        verify(acknowledgment, never()).acknowledge(); // Ensure message is NOT acked on error (so Kafka redelivers)
    }
}
