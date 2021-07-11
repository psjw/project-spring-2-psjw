package com.psjw.thisbox.controllers;

import com.psjw.thisbox.applications.AuthenticationService;
import com.psjw.thisbox.dto.SessionRequestData;
import com.psjw.thisbox.dto.SessionResponseData;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 토큰을 발급한다.
 */
@RestController
@RequestMapping("/session")
@CrossOrigin
public class SessionController {
    private AuthenticationService authenticationService;

    public SessionController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * AccessToken을 발급한다.
     * @param sessionRequestData
     * @return
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SessionResponseData login(@Valid @RequestBody SessionRequestData sessionRequestData){
        String email = sessionRequestData.getUserEmail();
        String password = sessionRequestData.getUserPassword();

        String accessToken  = authenticationService.login(email, password);

        return SessionResponseData.builder()
                .accessToken(accessToken)
                .build();
    }
}
