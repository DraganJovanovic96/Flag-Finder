# FlagFinder Multiplayer System

## Overview

The FlagFinder multiplayer system allows two players to compete in real-time flag guessing games. The system uses a room-based architecture where:

- **Rooms** are stored in-memory only and manage active game sessions
- **Games** are stored in the database for persistence and history

## Architecture

### Room vs Game Models

#### Room (In-Memory)
- Stored in `ConcurrentHashMap` in `RoomService`
- Short-lived: exists only during matchmaking & active gameplay
- Tracks host, guest, status, creation time
- Removed when game ends or a player disconnects

#### Game (Database)
- Stored in database for match history, leaderboards, and stats
- Created when match ends
- Includes players, scores, timestamps, winner

## API Endpoints

### Room Management

#### Create Room
```http
POST /api/rooms
Content-Type: application/json

{
  "hostUserName": "player1"
}
```

#### Get Room
```http
GET /api/rooms/{roomId}
```

#### Get All Rooms
```http
GET /api/rooms
```

#### Get User's Rooms
```http
GET /api/rooms/user/{userName}
```

#### Join Room
```http
POST /api/rooms/{roomId}/join
Content-Type: application/json

{
  "guestUserName": "player2"
}
```

#### Invite Friend
```http
POST /api/rooms/{roomId}/invite
Content-Type: application/json

{
  "hostUserName": "player1",
  "friendUserName": "player2"
}
```

#### Start Game
```http
POST /api/rooms/{roomId}/start
```

#### Complete Game
```http
POST /api/rooms/{roomId}/complete
```

#### Cancel Room
```http
POST /api/rooms/{roomId}/cancel
```

#### Delete Room
```http
DELETE /api/rooms/{roomId}
```

#### Check Room Exists
```http
GET /api/rooms/{roomId}/exists
```

#### Check User in Room
```http
GET /api/rooms/{roomId}/user/{userName}/in-room
```

### Game Management

#### Save Completed Game
```http
POST /api/games/completed
Content-Type: application/json

{
  "roomId": "uuid",
  "hostUserName": "player1",
  "guestUserName": "player2",
  "winnerUserName": "player1",
  "hostScore": 5,
  "guestScore": 3,
  "totalRounds": 10,
  "startedAt": "2024-01-01T10:00:00",
  "endedAt": "2024-01-01T10:15:00"
}
```

#### Get Game
```http
GET /api/games/{gameId}
```

#### Get User's Games
```http
GET /api/games/user/{userName}
```

#### Get All Completed Games
```http
GET /api/games/completed
```

## Data Models

### RoomDto
```java
{
  "roomId": "uuid",
  "hostUserName": "string",
  "guestUserName": "string",
  "status": "WAITING_FOR_GUEST|GAME_IN_PROGRESS|GAME_COMPLETED|CANCELLED",
  "createdAt": "timestamp",
  "gameStartedAt": "timestamp",
  "gameEndedAt": "timestamp"
}
```

### Game Entity
```java
{
  "id": "uuid",
  "users": ["User"],
  "rounds": ["Round"],
  "startedAt": "timestamp",
  "endedAt": "timestamp",
  "winnerUserName": "string",
  "hostScore": "integer",
  "guestScore": "integer",
  "totalRounds": "integer",
  "status": "IN_PROGRESS|COMPLETED|CANCELLED"
}
```

## Flow

1. **Player A creates room** (REST POST /api/rooms)
2. **Player A invites Player B** (REST POST /api/rooms/{roomId}/invite)
3. **Player B joins room** (REST POST /api/rooms/{roomId}/join)
4. **Game starts** (REST POST /api/rooms/{roomId}/start)
5. **Gameplay occurs** (Future: WebSocket for real-time gameplay)
6. **Game ends** (REST POST /api/rooms/{roomId}/complete)
7. **Game saved to database** (Automatic)
8. **Room deleted from memory** (Manual or automatic cleanup)

## Key Features

### Room Management
- ✅ Create rooms
- ✅ Join rooms
- ✅ Invite friends
- ✅ Start games
- ✅ Complete games
- ✅ Cancel rooms
- ✅ Room status tracking
- ✅ User validation

### Game Persistence
- ✅ Save completed games to database
- ✅ Game history tracking
- ✅ Player statistics
- ✅ Winner determination
- ✅ Score tracking

### Validation
- ✅ User existence validation
- ✅ Room availability checks
- ✅ Host-only operations
- ✅ State transition validation

## Future Enhancements

### WebSocket Integration
- Real-time game invites
- Live gameplay updates
- Player presence detection
- Instant room updates

### Game Logic
- Flag guessing mechanics
- Score calculation
- Round management
- Timer implementation

### Advanced Features
- Tournament system
- Leaderboards
- Achievement system
- Spectator mode

## Database Schema

### Games Table
```sql
CREATE TABLE games (
    id UUID PRIMARY KEY,
    started_at TIMESTAMP,
    ended_at TIMESTAMP,
    winner_user_name VARCHAR(255),
    host_score INTEGER,
    guest_score INTEGER,
    total_rounds INTEGER,
    game_status VARCHAR(50),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);
```

### User-Game Relationship
```sql
CREATE TABLE user_games (
    game_id UUID REFERENCES games(id),
    user_id UUID REFERENCES users(id),
    PRIMARY KEY (game_id, user_id)
);
```

## Error Handling

The system includes comprehensive error handling for:
- Invalid room IDs
- Non-existent users
- Invalid state transitions
- Unauthorized operations
- Database errors

## Logging

All operations are logged with appropriate levels:
- INFO: Normal operations
- WARN: Deprecated methods
- ERROR: Failed operations

## Security Considerations

- User validation on all operations
- Host-only operation restrictions
- Input validation and sanitization
- Proper error message handling 