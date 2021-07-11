package com.psjw.thisbox.dto;

import com.github.dozermapper.core.Mapping;
import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegistrationData {
    @Mapping("userEmail")
    @Email(message = "이메일 형식이 아닙니다.")
    @NotBlank
    private String userEmail;

    @Mapping("userPassword")
    @Size(min = 4, max = 1024)
    @NotBlank
    private String userPassword;

    @Mapping("userName")
    @NotBlank
    private String userName;

    @Mapping("userPhone")
    @Pattern(regexp = "[0-9]{10,11}", message = "10~11자리의 숫자만 입력가능합니다")
    private String userPhone;

    @Mapping("createDate")
    private LocalDateTime createDate;

    @Mapping("createById")
    private Long createById;

    @Mapping("updateDate")
    private LocalDateTime updateDate;

    @Mapping("updateById")
    private Long updateById;
}
