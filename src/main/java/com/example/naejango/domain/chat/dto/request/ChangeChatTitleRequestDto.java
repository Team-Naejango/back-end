package com.example.naejango.domain.chat.dto.request;

import lombok.*;
import org.hibernate.validator.constraints.Length;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ChangeChatTitleRequestDto {
    @Length(min = 2, max = 15)
    private String title;
}
