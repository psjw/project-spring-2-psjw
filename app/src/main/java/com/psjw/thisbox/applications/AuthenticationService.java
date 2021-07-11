package com.psjw.thisbox.applications;

import com.psjw.thisbox.domain.Role;
import com.psjw.thisbox.domain.RoleRepository;
import com.psjw.thisbox.domain.User;
import com.psjw.thisbox.domain.UserRepository;
import com.psjw.thisbox.exceptions.LoginFailException;
import com.psjw.thisbox.utils.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(UserRepository userRepository, RoleRepository roleRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public String login(String email, String password) {
        User foundUser = userRepository.findByUserEmail(email).
                orElseThrow(()-> new LoginFailException(email));
        if(!foundUser.authenticate(password,passwordEncoder)){
            throw new LoginFailException(email);
        }
        return jwtUtil.encode(foundUser.getUserId());
    }

    public Long parseToken(String accessToken){
        return jwtUtil.decode(accessToken)
                .get("userId", Long.class);
    }

    public List<Role> roles(Long userId) {
        return roleRepository.findAllByUserId(userId);
    }
}
