package com.psjw.thisbox.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class SessionRequestData {
    @NotBlank
    @Email
    private String userEmail;

    @NotBlank
    private String userPassword;
}
