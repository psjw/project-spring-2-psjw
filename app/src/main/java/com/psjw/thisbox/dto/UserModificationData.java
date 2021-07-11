package com.psjw.thisbox.dto;


import com.github.dozermapper.core.Mapping;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserModificationData {
    @NotBlank
    @Mapping("userName")
    private  String userName;

    @NotBlank
    @Size(min = 4, max = 1024)
    @Mapping("userPassword")
    private String userPassword;

    @Mapping("userPhone")
    @Pattern(regexp = "[0-9]{10,11}", message = "10~11자리의 숫자만 입력가능합니다")
    private String userPhone;
}
