package com.psjw.thisbox.domain;

import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class User {
    @Id
    @GeneratedValue
    private Long userId;

    @Builder.Default
    private String userPassword = "";
    @Builder.Default
    private String userName = "";
    @Builder.Default
    private String userEmail = "";
    @Builder.Default
    private String userPhone = "";
    private LocalDateTime createDate;
    private Long createById;
    private LocalDateTime updateDate;
    private Long updateById;
    @Builder.Default
    private boolean deleted = false;


    public void isWithdrawal() {
        deleted = true;
    }

    public boolean authenticate(String userPassword, PasswordEncoder passwordEncoder) {
        return !deleted && passwordEncoder.matches(userPassword, this.userPassword);
    }

    public void changePassword(String userPassword, PasswordEncoder passwordEncoder) {
        this.userPassword = passwordEncoder.encode(userPassword);
    }

    public void changeWith(User source) {
        this.userEmail = source.getUserEmail();
        this.userName = source.getUserName();
        this.userPhone = source.getUserPhone();
        this.updateById = source.userId;
        this.updateDate = LocalDateTime.now();
    }

    public void updateUserInfo(Long createById, Long updateById) {
        if (createById != null) {
            this.createDate = LocalDateTime.now();
            this.createById = createById;
        }
        this.updateDate = LocalDateTime.now();
        this.updateById = updateById;
    }
}
