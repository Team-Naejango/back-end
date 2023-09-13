package com.example.naejango.domain.chat.dto.request;

import lombok.*;

import javax.validation.constraints.Size;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ChangeChatTitleRequestDto {
    @Size(min = 2, max = 15)
    private String title;
}
