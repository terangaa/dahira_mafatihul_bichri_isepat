package com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.repository;

import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByMemberId(Long memberId);
    boolean existsByEmail(String email);
}
