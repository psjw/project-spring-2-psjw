package com.psjw.thisbox.infra;

import com.psjw.thisbox.domain.Role;
import com.psjw.thisbox.domain.RoleRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface JpaRoleRepository extends CrudRepository<Role, Long>, RoleRepository {
    List<Role> findAllByUserId(Long userId);

    Role save(Role role);
}
