package com.flagfinder.config;

import org.mapstruct.MapperConfig;
import org.mapstruct.MappingInheritanceStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This configuration class sets the {@code componentModel} to {@code "spring"}, which enables MapStruct to generate
 * Spring components for mapper interfaces, and the {@code unmappedTargetPolicy} to {@code ReportingPolicy.IGNORE},
 * which instructs MapStruct to ignore any unmapped target properties.
 * It also sets the {@code mappingInheritanceStrategy} to {@code MappingInheritanceStrategy.AUTO_INHERIT_FROM_CONFIG},
 * which allows MapStruct to inherit mapping configurations from parent configuration classes.
 * You can add any additional configuration for MapStruct.
 *
 * @author Dragan Jovanovic
 * @version 1.0
 * @since 1.0
 */
@Configuration
@MapperConfig(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        mappingInheritanceStrategy = MappingInheritanceStrategy.AUTO_INHERIT_FROM_CONFIG
)
public class MapStructConfig {
    /**
     * Creates a UserMapper bean for dependency injection.
     * 
     * @return configured UserMapper instance
     */
    @Bean
    public com.flagfinder.mapper.UserMapper userMapper() {
        return Mappers.getMapper(com.flagfinder.mapper.UserMapper.class);
    }

    /**
     * Creates a FriendshipMapper bean for dependency injection.
     * 
     * @return configured FriendshipMapper instance
     */
    @Bean
    public com.flagfinder.mapper.FriendshipMapper friendshipMapper() {
        return Mappers.getMapper(com.flagfinder.mapper.FriendshipMapper.class);
    }

    /**
     * Creates a RoomMapper bean for dependency injection.
     * 
     * @return configured RoomMapper instance
     */
    @Bean
    public com.flagfinder.mapper.RoomMapper roomMapper() {
        return Mappers.getMapper(com.flagfinder.mapper.RoomMapper.class);
    }

    /**
     * Creates a GameMapper bean for dependency injection.
     * 
     * @return configured GameMapper instance
     */
    @Bean
    public com.flagfinder.mapper.GameMapper gameMapper() {
        return Mappers.getMapper(com.flagfinder.mapper.GameMapper.class);
    }

    /**
     * Creates a RoundMapper bean for dependency injection.
     * 
     * @return configured RoundMapper instance
     */
    @Bean
    public com.flagfinder.mapper.RoundMapper roundMapper() {
        return Mappers.getMapper(com.flagfinder.mapper.RoundMapper.class);
    }

    /**
     * Creates a GuessMapper bean for dependency injection.
     * 
     * @return configured GuessMapper instance
     */
    @Bean
    public com.flagfinder.mapper.GuessMapper guessMapper() {
        return Mappers.getMapper(com.flagfinder.mapper.GuessMapper.class);
    }

    /**
     * Creates a SinglePlayerRoomMapper bean for dependency injection.
     * 
     * @return configured SinglePlayerRoomMapper instance
     */
    @Bean
    public com.flagfinder.mapper.SinglePlayerRoomMapper singlePlayerRoomMapper() {
        return Mappers.getMapper(com.flagfinder.mapper.SinglePlayerRoomMapper.class);
    }

    /**
     * Creates a SinglePlayerGameMapper bean for dependency injection.
     * 
     * @return configured SinglePlayerGameMapper instance
     */
    @Bean
    public com.flagfinder.mapper.SinglePlayerGameMapper singlePlayerGameMapper() {
        return Mappers.getMapper(com.flagfinder.mapper.SinglePlayerGameMapper.class);
    }

    /**
     * Creates a SinglePlayerRoundMapper bean for dependency injection.
     * 
     * @return configured SinglePlayerRoundMapper instance
     */
    @Bean
    public com.flagfinder.mapper.SinglePlayerRoundMapper singlePlayerRoundMapper() {
        return Mappers.getMapper(com.flagfinder.mapper.SinglePlayerRoundMapper.class);
    }
}
