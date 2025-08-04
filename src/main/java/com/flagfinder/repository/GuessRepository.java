package com.flagfinder.repository;

import com.flagfinder.model.Guess;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GuessRepository extends JpaRepository<Guess,UUID> {

  List<Guess> findAllByRoundIdAndUserId(UUID roundId, UUID userId);
}
