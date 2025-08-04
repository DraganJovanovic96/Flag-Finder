package com.flagfinder.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "games")
@EqualsAndHashCode(callSuper = false)
public class Game extends BaseEntity{

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_games",
            joinColumns = @JoinColumn(name = "game_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> users;

    @OneToMany(mappedBy = "game")
    private List<Round> rounds = new ArrayList<>();
}
