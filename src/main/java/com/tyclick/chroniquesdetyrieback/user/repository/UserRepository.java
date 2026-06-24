package com.tyclick.chroniquesdetyrieback.user.repository;

import com.tyclick.chroniquesdetyrieback.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    // Custom query methods to find users by email or username, and to check for existence
    Optional<User> findByemail(String email);
    Optional<User> findByusername(String username);
    boolean existsByemail(String email);
    boolean existsByusername(String username);

}
