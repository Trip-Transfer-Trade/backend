package com.example.module_exchange.redisData;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.File;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() throws Exception {
        // 1) SSLContext 생성 (Keystore 적용)
        SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial(
                        new File("/usr/local/openjdk-17/lib/security/cacerts"), // 실제 Keystore 경로
                        "changeit".toCharArray()                                 // Keystore 비밀번호
                )
                .build();

        // 2) SSLConnectionSocketFactory 생성
        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext);

        // 3) PoolingHttpClientConnectionManager 생성 (HttpClient 5.x)
        //    => setDefaultTlsStrategy(...) 대신 setSSLSocketFactory(...) 사용
        var connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(sslSocketFactory)
                .setMaxConnTotal(50)     // 전체 커넥션 최대 50개
                .setMaxConnPerRoute(20)  // 호스트당 최대 20개
                .build();

        // 4) 요청 타임아웃 설정
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(2))   // 연결 타임아웃 (2초)
                .setResponseTimeout(Timeout.ofSeconds(5))  // 응답 타임아웃 (5초)
                .build();

        // 5) HttpClient 5.x 생성
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                // 필요하다면 추가 설정
                // .disableRedirectHandling()
                // .disableCookieManagement()
                .build();

        // 6) RestTemplate 생성
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return new RestTemplate(factory);
    }
}