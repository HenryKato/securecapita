package io.hkfullstack.securecapita.repository;

import io.hkfullstack.securecapita.model.Role;

import java.util.Collection;

public interface RoleRepository<T extends Role> {

    /* Basic CRUD Operations */
    T createRole(T role);
    T getRole(Long id);
    Collection<T> getRoles(int page, int pageSize);
    void updateRole(T user);
    Boolean deleteRole(Long id);

    /* More Complex Operations */
    void addRoleToUser(Long userId, String roleName);
    Role getRoleByUserId(Long userId);
    Role getRoleByUserEmail(String email);
    void updateUserRole(Long userId, String roleName);

}
