package com.example.naejango.global.common.api;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@RestController
@RequiredArgsConstructor
public class HealthCheck {
    private final DataSource dataSource;
    private final DataSourceProperties dataSourceProperties;

    /** EC2 서버 접속 테스트를 위한 임시 API */
    @GetMapping("/healthcheck")
    public ResponseEntity<Void> healthCheck() {
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @GetMapping("/DBconnection")
    public String DBConnectionCheck() {
        try {
            Connection connection = dataSource.getConnection();
            if (connection != null) {
                return "<h1>DB 연결 성공<h1>" +
                        "DBtype = " + dataSourceProperties.getDriverClassName() + "</br>" +
                        "url = " + dataSourceProperties.getUrl();

            } else {
                return "DB 연결 실패 " ;
            }
        } catch (SQLException e) {
            return "DB 연결 오류: " + e.getMessage();
        }
    }
}