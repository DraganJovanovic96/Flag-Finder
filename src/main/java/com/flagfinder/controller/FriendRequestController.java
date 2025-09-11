package com.flagfinder.controller;

import com.flagfinder.dto.FriendRequestResponseDto;
import com.flagfinder.dto.FriendshipDto;
import com.flagfinder.dto.SendUserNameDto;
import com.flagfinder.service.FriendshipService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/friends")
@CrossOrigin
public class FriendRequestController {

    /**
     * The service used to for services.
     */
    private final FriendshipService friendshipService;

    /**
     * Creates a new service using the information provided in the {@code ServiceCreateDto}
     * and returns a ResponseEntity object with status code 201 (Created) and the saved ServiceDto
     * object in the response body.
     *
     * @param sendUserNameDto the DTO containing the information for the new service to be created
     * @return a ResponseEntity object with status code 201 (Created) and the saved ServiceDto
     * object in the response body
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, path = "/send-friend-request")
    @PreAuthorize("hasAnyAuthority('admin:create', 'user:create')")
    @ApiOperation(value = "Send friend request")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully sent friend request .", response = SendUserNameDto.class),
            @ApiResponse(code = 409, message = "Friend request already sent.")
    })
    public ResponseEntity<FriendshipDto> sendFriendRequest(@Valid @RequestBody SendUserNameDto
                                                                   sendUserNameDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(friendshipService.sendFriendRequest(sendUserNameDto));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, path = "/request")
    @PreAuthorize("hasAnyAuthority('admin:create', 'user:create')")
    public ResponseEntity<Void> sendFriendRequestSimple(@Valid @RequestBody SendUserNameDto sendUserNameDto) {
        friendshipService.sendFriendRequest(sendUserNameDto);
        return ResponseEntity.ok().build();
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, path = "/friend-request-response")
    @PreAuthorize("hasAnyAuthority('admin:create', 'user:create')")
    @ApiOperation(value = "Friend request response")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully answered friend request.", response = FriendRequestResponseDto.class),
            @ApiResponse(code = 404, message = "Friend request missing.")
    })
    public ResponseEntity<FriendshipDto> friendRequestResponse(@Valid @RequestBody FriendRequestResponseDto friendRequestResponseDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(friendshipService.respondToFriendRequest(friendRequestResponseDto));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, path = "/respond")
    @PreAuthorize("hasAnyAuthority('admin:create', 'user:create')")
    public ResponseEntity<Void> respondToFriendRequestSimple(@Valid @RequestBody FriendRequestResponseDto friendRequestResponseDto) {
        friendshipService.respondToFriendRequest(friendRequestResponseDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/requests")
    @PreAuthorize("hasAnyAuthority('admin:read', 'user:read')")
    @ApiOperation(value = "Friend requests")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully fetched friend requests.", response = FriendRequestResponseDto.class),
    })
    public  ResponseEntity<List<FriendshipDto>> getFriendRequests(@RequestParam(value = "page", defaultValue = "0") int page,
                                                                  @RequestParam(value = "pageSize", defaultValue = "5") @Min(1) int pageSize) {
        Page<FriendshipDto> resultPage = friendshipService.findAllFriendShipRequests(page, pageSize);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Items", String.valueOf(resultPage.getTotalElements()));
        headers.add("X-Total-Pages", String.valueOf(resultPage.getTotalPages()));
        headers.add("X-Current-Page", String.valueOf(resultPage.getNumber()));

        return new ResponseEntity<>(resultPage.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyAuthority('admin:read', 'user:read')")
    @ApiOperation(value = "Friends")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully fetched friends.", response = FriendRequestResponseDto.class),
    })
    public  ResponseEntity<List<FriendshipDto>> getFriends(@RequestParam(value = "page", defaultValue = "0") int page,
                                                                  @RequestParam(value = "pageSize", defaultValue = "5") @Min(1) int pageSize) {
        Page<FriendshipDto> resultPage = friendshipService.findAllFriends(page, pageSize);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Items", String.valueOf(resultPage.getTotalElements()));
        headers.add("X-Total-Pages", String.valueOf(resultPage.getTotalPages()));
        headers.add("X-Current-Page", String.valueOf(resultPage.getNumber()));

        return new ResponseEntity<>(resultPage.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/requests/received")
    @PreAuthorize("hasAnyAuthority('admin:read', 'user:read')")
    public ResponseEntity<List<FriendshipDto>> getReceivedFriendRequests() {
        Page<FriendshipDto> resultPage = friendshipService.findAllFriendShipRequests(0, 100);
        return ResponseEntity.ok(resultPage.getContent());
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/requests/sent")
    @PreAuthorize("hasAnyAuthority('admin:read', 'user:read')")
    public ResponseEntity<List<FriendshipDto>> getSentFriendRequests() {
        // For now, return empty list since there's no service method for sent requests
        // This would need to be implemented in the service layer
        return ResponseEntity.ok(java.util.Collections.emptyList());
    }

    @DeleteMapping(path = "/{friendUsername}")
    @PreAuthorize("hasAnyAuthority('admin:delete', 'user:delete')")
    public ResponseEntity<Void> removeFriend(@PathVariable String friendUsername) {
        friendshipService.removeFriend(friendUsername);
        return ResponseEntity.ok().build();
    }
}
