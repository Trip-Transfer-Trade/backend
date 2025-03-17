package com.example.module_exchange.redisData;

import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {package com.example.module_exchange.redisData;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

    @Configuration
    public class RestTemplateConfig {

        @Bean
        public RestTemplate restTemplate() {
            return new RestTemplate();
        }
    }

    @Bean
    public RestTemplate restTemplate() {
        // ✅ 올바른 HttpClient5 ConnectionManager 사용
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setDefaultMaxPerRoute(20); // 호스트당 최대 20개 연결
        connectionManager.setMaxTotal(100); // 최대 100개 커넥션 풀

        // ✅ HttpClient5에서 올바른 빌더 방식 적용
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .evictExpiredConnections() // 만료된 커넥션 정리
                .evictIdleConnections(Timeout.ofSeconds(30)) // 30초 이상 유휴 커넥션 제거
                .disableCookieManagement() // 필요하지 않으면 쿠키 관리 비활성화
                .build();

        // ✅ HttpClient5에 맞는 RequestFactory 적용
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return new RestTemplate(factory);
    }
}
