package com.cth.sdm.repository;

import com.cth.sdm.model.SdmUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SdmUserRepository extends JpaRepository<SdmUser, String> {
    Optional<SdmUser> findByPasswordResetToken(String token);
}
