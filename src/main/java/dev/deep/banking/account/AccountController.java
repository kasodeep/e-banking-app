package dev.deep.banking.account;

import dev.deep.banking.account.request.AccountTransactionPinUpdateModel;
import dev.deep.banking.account.response.AccountOverviewResponse;
import dev.deep.banking.config.JwtService;
import dev.deep.banking.exception.ResourceNotFoundException;
import dev.deep.banking.universal.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    private final JwtService jwtService;

    /**
     * This controller fetches the user account overview by getting the userId from the JWT token.
     */
    @GetMapping("/overview")
    public ResponseEntity<ApiResponse> getUserAccountOverview(@RequestHeader("Authorization") String jwt) {
        try {
            AccountOverviewResponse response = accountService.generateAccountOverviewByUserId(jwtService.extractUserIdFromToken(jwt));
            return new ResponseEntity<>(new ApiResponse("User account overview", response), HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(new ApiResponse(e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * This controller allows user to close their account by getting the userId from the JWT and the relieving reason from the request body.
     */
    @DeleteMapping("/close")
    public ResponseEntity<ApiResponse> closeAccount(@RequestHeader("Authorization") String jwt) {
        accountService.closeAccount(jwtService.extractUserIdFromToken(jwt));
        return new ResponseEntity<>(new ApiResponse("Account closed successfully"), HttpStatus.OK);
    }

    @PutMapping("/transaction-pin")
    public ResponseEntity<ApiResponse> updateAccountTransactionPin(
            @RequestHeader("Authorization") String jwt,
            @RequestBody @Validated AccountTransactionPinUpdateModel pinUpdateModel) {

        accountService.updateAccountTransactionPin(jwtService.extractUserIdFromToken(jwt), pinUpdateModel);
        return new ResponseEntity<>(new ApiResponse("Transaction pin set"), HttpStatus.OK);
    }
}
