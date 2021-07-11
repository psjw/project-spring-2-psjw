package com.psjw.thisbox.controllers;

import com.psjw.thisbox.applications.UserService;
import com.psjw.thisbox.domain.User;
import com.psjw.thisbox.dto.UserModificationData;
import com.psjw.thisbox.dto.UserRegistrationData;
import com.psjw.thisbox.dto.UserResponseData;
import com.psjw.thisbox.security.UserAuthentication;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.nio.file.AccessDeniedException;
import java.util.List;

/**
 * User CRUD(Create, Select, Update, Delete)
 */
@RequestMapping("/users")
@RestController
@CrossOrigin
public class UserController {
    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated() and hasAnyAuthority('ADMIN')")
    public List<User> getUsers() {
        return userService.findAllUsers();
    }

    @GetMapping("{id}")
    @PreAuthorize("isAuthenticated() and hasAnyAuthority('USER','ADMIN')")
    public UserResponseData getUser(@PathVariable Long id) {
        User findUser = userService.findUser(id);
        return getUserResponseData(findUser);
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseData createUser(
            @RequestBody @Valid UserRegistrationData userRegistrationData) {
        User user = userService.createUser(userRegistrationData);
        return getUserResponseData(user);
    }

    @PatchMapping("{id}")
    @PreAuthorize("isAuthenticated() and hasAnyAuthority('USER','ADMIN')")
    public UserResponseData updateUser(@PathVariable Long id
            , @RequestBody @Valid UserModificationData userModificationData
            , UserAuthentication userAuthentication) throws AccessDeniedException {
        Long userId = userAuthentication.getUserId();
        User updateUser = userService.updateUser(id, userModificationData, userId);
        return getUserResponseData(updateUser);
    }

    @DeleteMapping("{id}")
    @PreAuthorize("isAuthenticated() and hasAnyAuthority('USER','ADMIN')")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    private UserResponseData getUserResponseData(User findUser) {
        if (findUser == null || findUser.isDeleted()) {
            return null;
        }

        return UserResponseData.builder()
                .userId(findUser.getUserId())
                .userEmail(findUser.getUserEmail())
                .userName(findUser.getUserName())
                .userPhone(findUser.getUserPhone())
                .createById(findUser.getCreateById())
                .createDate(findUser.getCreateDate())
                .updateById(findUser.getUpdateById())
                .updateDate(findUser.getUpdateDate())
                .build();
    }

}
