package com.flagfinder.repository;

import com.flagfinder.model.Round;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoundRepository extends JpaRepository<Round, UUID> {

    Optional<Round> findOneById(UUID serviceId);
}
