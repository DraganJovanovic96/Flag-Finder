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
     * The service used for friendship operations.
     */
    private final FriendshipService friendshipService;

    /**
     * Sends a friend request to another user.
     *
     * @param sendUserNameDto the DTO containing the username to send friend request to
     * @return a ResponseEntity object with status code 201 (Created) and the FriendshipDto object
     * @throws ResponseStatusException if friend request already exists or user not found
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

    /**
     * Responds to a friend request (accept or decline).
     *
     * @param friendRequestResponseDto the DTO containing the response to the friend request
     * @return a ResponseEntity object with status code 201 (Created) and the FriendshipDto object
     * @throws ResponseStatusException if friend request is not found
     */
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

    /**
     * Retrieves paginated friend requests for the authenticated user.
     *
     * @param page the page number (0-based)
     * @param pageSize the number of items per page
     * @return a ResponseEntity with paginated friend requests and pagination headers
     */
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

    /**
     * Retrieves paginated friends list for the authenticated user.
     *
     * @param page the page number (0-based)
     * @param pageSize the number of items per page
     * @return a ResponseEntity with paginated friends list and pagination headers
     */
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

    /**
     * Removes a friend from the authenticated user's friends list.
     *
     * @param friendUsername the username of the friend to remove
     * @return a ResponseEntity with status code 200 (OK)
     * @throws ResponseStatusException if friend is not found
     */
    @DeleteMapping(path = "/{friendUsername}")
    @PreAuthorize("hasAnyAuthority('admin:delete', 'user:delete')")
    @ApiOperation(value = "Remove friend")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully removed friend."),
            @ApiResponse(code = 404, message = "Friend not found.")
    })
    public ResponseEntity<Void> removeFriend(@PathVariable String friendUsername) {
        friendshipService.removeFriend(friendUsername);
        return ResponseEntity.ok().build();
    }
}
