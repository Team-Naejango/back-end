package com.example.naejango.domain.common;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class CommonResponseDto<T> {
    private String Message;
    private T result;
}
