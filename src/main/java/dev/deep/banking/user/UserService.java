package dev.deep.banking.user;

import dev.deep.banking.account.Account;
import dev.deep.banking.account.AccountService;
import dev.deep.banking.config.JwtService;
import dev.deep.banking.exception.InvalidAuthenticationException;
import dev.deep.banking.exception.ResourceExistsException;
import dev.deep.banking.exception.ResourceNotFoundException;
import dev.deep.banking.exception.ValueMismatchException;
import dev.deep.banking.user.request.ChangePasswordRequest;
import dev.deep.banking.user.request.UserAuthenticationRequest;
import dev.deep.banking.user.request.UserRegistrationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    private final UserRepository userRepository;

    private final JwtService jwtService;

    private final AccountService accountService;

    public void updateUser(User existingUser) {
        existingUser.setUpdatedAt(LocalDateTime.now());
        userRepository.save(existingUser);
    }

    /**
     * Created new account for the user as well.
     */
    @Transactional
    public void createNewUser(UserRegistrationRequest userRegistrationRequest) {
        if (userRepository.existsByEmailAddress(userRegistrationRequest.emailAddress()))
            throw new ResourceExistsException("Email address is taken");

        User newUser = new User(
                userRegistrationRequest.fullName(),
                userRegistrationRequest.emailAddress(),
                encodePassword(userRegistrationRequest.password()),
                true,
                userRegistrationRequest.phoneNumber());

        userRepository.save(newUser);
        accountService.createAccount(new Account(newUser.getId()));
    }

    public String authenticateUser(UserAuthenticationRequest requests) {
        User existingUser = getUserByEmailAddress(requests.emailAddress());

        if (passwordMatches(requests.password(), existingUser.getPassword())) {
            Map<String, Object> claims = Map.of("userId", existingUser.getId());
            return jwtService.generateToken(claims, existingUser);
        }

        throw new InvalidAuthenticationException("Invalid username or password");
    }

    public void changeUserPassword(ChangePasswordRequest request, Integer userId) {
        User existingUser = getUserByUserId(userId);

        if (!bCryptPasswordEncoder.matches(request.oldPassword(), existingUser.getPassword()))
            throw new ValueMismatchException("Old password does not match");

        existingUser.setPassword(bCryptPasswordEncoder.encode(request.newPassword()));
        updateUser(existingUser);
    }

    public User getUserByUserId(int userId) {
        Optional<User> existingUser = userRepository.findById(userId);
        if (existingUser.isEmpty()) throw new ResourceNotFoundException("User not found");
        return existingUser.get();
    }

    public User getUserByEmailAddress(String emailAddress) {
        Optional<User> existingUser = userRepository.findByEmailAddress(emailAddress);
        if (existingUser.isEmpty()) throw new ResourceNotFoundException("User not found");
        return existingUser.get();
    }

    private String encodePassword(String password) {
        return bCryptPasswordEncoder.encode(password);
    }

    private boolean passwordMatches(String rawPassword, String encodedPassword) {
        return bCryptPasswordEncoder.matches(rawPassword, encodedPassword);
    }
}
