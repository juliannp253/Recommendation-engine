package com.project.recommendation_engine.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationAgentServiceTest {

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    private RecommendationAgentService service;

    @BeforeEach
    void setUp() {
        service = new RecommendationAgentService(cacheManager);
        ReflectionTestUtils.setField(service, "pythonCommand", "python3");
        ReflectionTestUtils.setField(service, "SCRIPT_PATH", "./python_agent/batch_processor.py");
    }

    @Test
    void triggerRecommendationForUser_buildsProcessBuilderCorrectlyAndEvictsCacheOnSuccess() throws Exception {
        when(cacheManager.getCache("userRecommendations")).thenReturn(cache);

        List<List<String>> capturedCommands = new ArrayList<>();

        try (MockedConstruction<ProcessBuilder> mockedPb = mockConstruction(ProcessBuilder.class,
                (mock, context) -> {
                    String[] cmd = (String[]) context.arguments().get(0);
                    capturedCommands.add(Arrays.asList(cmd));

                    Process mockProcess = mock(Process.class);
                    when(mock.start()).thenReturn(mockProcess);
                    when(mockProcess.getInputStream()).thenReturn(new ByteArrayInputStream("Step finished".getBytes()));
                    when(mockProcess.waitFor()).thenReturn(0);
                })) {

            service.triggerRecommendationForUser("user-123");

            assertEquals(1, mockedPb.constructed().size());
            assertEquals(1, capturedCommands.size());
            assertEquals(
                    List.of("python3", "./python_agent/batch_processor.py", "--user_id", "user-123"),
                    capturedCommands.get(0)
            );

            ProcessBuilder constructedPb = mockedPb.constructed().get(0);
            verify(constructedPb).redirectErrorStream(true);
            verify(constructedPb).start();
            verify(cache).evict("user-123");
        }
    }

    @Test
    void triggerRecommendationForUser_doesNotEvictCacheWhenProcessFails() throws Exception {
        try (MockedConstruction<ProcessBuilder> mockedPb = mockConstruction(ProcessBuilder.class,
                (mock, context) -> {
                    Process mockProcess = mock(Process.class);
                    when(mock.start()).thenReturn(mockProcess);
                    when(mockProcess.getInputStream()).thenReturn(new ByteArrayInputStream("Error".getBytes()));
                    when(mockProcess.waitFor()).thenReturn(1);
                })) {

            service.triggerRecommendationForUser("user-123");

            assertEquals(1, mockedPb.constructed().size());
            verify(cacheManager, never()).getCache(anyString());
            verify(cache, never()).evict(anyString());
        }
    }

    @Test
    void triggerRecommendationForUser_handlesProcessStartExceptionGracefully() throws Exception {
        try (MockedConstruction<ProcessBuilder> mockedPb = mockConstruction(ProcessBuilder.class,
                (mock, context) -> {
                    when(mock.start()).thenThrow(new IOException("Cannot run program"));
                })) {

            service.triggerRecommendationForUser("user-123");

            assertEquals(1, mockedPb.constructed().size());
            verify(cacheManager, never()).getCache(anyString());
        }
    }
}
