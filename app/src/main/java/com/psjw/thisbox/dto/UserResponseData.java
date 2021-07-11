package com.psjw.thisbox.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseData {
    private Long userId;

    private String userName;

    private String userEmail;

    private String userPhone;

    private LocalDateTime createDate;

    private Long createById;

    private LocalDateTime updateDate;

    private Long updateById;

    @Builder.Default
    private boolean deleted=false;
}
