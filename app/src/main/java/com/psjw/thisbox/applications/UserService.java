package com.psjw.thisbox.applications;

import com.github.dozermapper.core.Mapper;
import com.psjw.thisbox.domain.*;
import com.psjw.thisbox.dto.UserModificationData;
import com.psjw.thisbox.dto.UserRegistrationData;
import com.psjw.thisbox.exceptions.UserEmailDuplicationException;
import com.psjw.thisbox.exceptions.UserNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final Mapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;


    public UserService(UserRepository userRepository, Mapper mapper, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public User findUser(Long id)  {
        return userRepository.findByUserIdAndDeletedIsFalse(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public User createUser(UserRegistrationData userRegistrationData) {
        String userEmail = userRegistrationData.getUserEmail();
        User foundUser = userRepository.findByUserEmail(userEmail)
                .orElse(null);
        if(foundUser != null){
            throw new UserEmailDuplicationException(userEmail);
        }
        User user = mapper.map(userRegistrationData, User.class);
        user.updateUserInfo(user.getUserId(),user.getUserId());
        user.changePassword(userRegistrationData.getUserPassword(), passwordEncoder);
        roleRepository.save(new Role(user.getUserId(),"USER"));
        return userRepository.save(user);
    }

    public User updateUser(Long id, UserModificationData modificationData
            , Long userId) throws AccessDeniedException {
        if(id != userId){
            throw new AccessDeniedException("Access Denied");
        }
        User foudnUser = findUser(id);
        foudnUser.updateUserInfo(null,foudnUser.getUserId());
        User source = mapper.map(modificationData, User.class);
        foudnUser.changeWith(source);
        return foudnUser;
    }

    public User deleteUser(Long id){
        User deletedUser = findUser(id);
        deletedUser.isWithdrawal();
        return deletedUser;
    }
}
