package dev.deep.banking.user;


import dev.deep.banking.config.JwtService;
import dev.deep.banking.universal.ApiResponse;
import dev.deep.banking.user.request.ChangePasswordRequest;
import dev.deep.banking.user.request.UserAuthenticationRequest;
import dev.deep.banking.user.request.UserRegistrationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    private final JwtService jwtService;

    @PostMapping()
    public ResponseEntity<ApiResponse> saveUser(@RequestBody @Validated UserRegistrationRequest request) {
        userService.createNewUser(request);
        return new ResponseEntity<>(new ApiResponse("User created successfully"), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> authenticateUser(@RequestBody @Validated UserAuthenticationRequest userAuthenticationRequests) {
        String jwt = userService.authenticateUser(userAuthenticationRequests);
        return new ResponseEntity<>(new ApiResponse("User logged in successfully", jwt), HttpStatus.OK);
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse> changeUserPassword(@RequestHeader("Authorization") String jwt, @RequestBody @Validated ChangePasswordRequest request) {
        userService.changeUserPassword(request, jwtService.extractUserIdFromToken(jwt));
        return new ResponseEntity<>(new ApiResponse("Password changed"), HttpStatus.OK);
    }
}

