package com.flagfinder.service;

import com.flagfinder.dto.SendUserNameDto;
import com.flagfinder.model.Game;

public interface GameService {

    Game inviteToPlay(SendUserNameDto sendUserNameDto);
}
