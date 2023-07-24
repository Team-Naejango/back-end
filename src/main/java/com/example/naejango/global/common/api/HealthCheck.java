package com.example.naejango.global.common.api;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HealthCheck {
    private final DataSourceProperties dataSourceProperties;

    /** EC2 서버 접속 테스트를 위한 임시 API */
    @GetMapping("/healthcheck")
    public ResponseEntity<Void> healthCheck() {
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @GetMapping("/dbcheck")
    public String DBCheck() {
        String driverClassName = dataSourceProperties.getDriverClassName();
        String url = dataSourceProperties.getUrl();
        return "DBtype = " + driverClassName + "</br>" +
                "url = " + url;
    }
}