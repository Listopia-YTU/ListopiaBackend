package com.savt.listopia.service;

import com.savt.listopia.exception.APIException;
import com.savt.listopia.exception.userException.UserNotAuthorizedException;
import com.savt.listopia.model.user.Session;
import com.savt.listopia.model.user.auth.PendingUser;
import com.savt.listopia.model.user.User;
import com.savt.listopia.model.user.UserRole;
import com.savt.listopia.model.user.auth.UserVerification;
import com.savt.listopia.payload.request.SignUpRequest;
import com.savt.listopia.repository.auth.PendingUserRepository;
import com.savt.listopia.repository.UserRepository;
import com.savt.listopia.repository.auth.UserVerificationRepository;
import com.savt.listopia.util.MailUtil;
import com.savt.listopia.util.PasswordUtil;
import com.savt.listopia.util.RandomUtil;
import com.savt.listopia.util.UserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;


@Service
public class AuthServiceImpl implements AuthService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserService userService;
    private final UserRepository userRepository;
    private final PendingUserRepository pendingUserRepository;
    private final UserVerificationRepository userVerificationRepository;
    private final SessionService sessionService;

    public AuthServiceImpl(UserService userService, UserRepository userRepository, PendingUserRepository pendingUserRepository, UserVerificationRepository userVerificationRepository, SessionService sessionService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.pendingUserRepository = pendingUserRepository;
        this.userVerificationRepository = userVerificationRepository;
        this.sessionService = sessionService;
    }

    @Override
    public void requireRoleOrThrow(UserRole role) {
        Long userId = userService.getCurrentUserIdOrThrow();
        User user = userRepository.findById(userId).orElseThrow(UserNotAuthorizedException::new);
        if ( !user.getRole().hasAtLeast(role) ) {
            throw new UserNotAuthorizedException();
        }
    }

    private String generateVerificationToken() {
        return RandomUtil.generateRandomString();
    }

    private static boolean isOlderThanMinutes(long timestampMillis, int minutes) {
        Instant timestampInstant = Instant.ofEpochMilli(timestampMillis);
        Instant now = Instant.now();
        Duration duration = Duration.between(timestampInstant, now);
        return duration.toMinutes() >= minutes;
    }

    private void sendUserMail(String mail, String token) {
        LOGGER.info("Sending verification user mail to {}", mail);
        // TODO: fix url pls
        String retUrl = "https://api.ensargok.com/api/v1/auth/verify?mail=" + mail + "&token=" + token;
        String mailContent = """
        Hello, this is your email verification token!
        
        Click this URL to verify your account:
        """ + retUrl;
        MailUtil.sendMail(
                mail,
                "Mail Verification Mail | Listopia",
                mailContent
        );
        LOGGER.warn("mailContent: {}", mailContent);
    }

    private void checkAndSendUserVerificationMail(String email) {
        // check if a token is sent within 15 minutes
        Optional<UserVerification> verification = userVerificationRepository.findByMail(email);
        if (verification.isPresent()) {
            UserVerification userVerification = verification.get();
            // check if 15 minues passed
            if (isOlderThanMinutes(userVerification.getSentAt(), 15)) {
                String token = generateVerificationToken();
                userVerification.setSentAt(System.currentTimeMillis());
                userVerification.setToken(token);
                UserVerification sent = userVerificationRepository.save(userVerification);
                sendUserMail(sent.getMail(), sent.getToken());
            }
        } else {
            // new user, send mail
            UserVerification userVerification = new UserVerification();
            userVerification.setToken(generateVerificationToken());
            userVerification.setSentAt(System.currentTimeMillis());
            userVerification.setMail(email);
            UserVerification sent = userVerificationRepository.save(userVerification);
            sendUserMail(sent.getMail(), sent.getToken());
        }
    }

    @Override
    public void handleSignUp(SignUpRequest signUpRequest) {
        // this should be on top so time base attacks won't work!
        String hashedPassword = PasswordUtil.hashPassword(signUpRequest.getPassword());

        if (userRepository.existsByUsernameLower(UserUtil.usernameToLowerCase(signUpRequest.getUsername())))
            throw new APIException("username_already_exists");

        if (userRepository.existsByEmail(signUpRequest.getEmail()))
            return;

        Optional<PendingUser> optUsernameCheck = pendingUserRepository.findByUsernameLower(UserUtil.usernameToLowerCase(signUpRequest.getUsername()));
        if (optUsernameCheck.isPresent()) {
            PendingUser pendingUser = optUsernameCheck.get();
            if (!pendingUser.getEmail().equalsIgnoreCase(signUpRequest.getEmail())) {
                throw new APIException("username_already_exists");
            }
        }

        Optional<PendingUser> optionalPendingUser = pendingUserRepository.findByEmail(signUpRequest.getEmail());
        PendingUser pendingUser = optionalPendingUser.orElseGet(PendingUser::new);

        pendingUser.setEmail(signUpRequest.getEmail());
        pendingUser.setUsername(signUpRequest.getUsername());
        pendingUser.setUsernameLower(UserUtil.usernameToLowerCase(signUpRequest.getUsername()));
        pendingUser.setFirstName(signUpRequest.getFirstName());
        pendingUser.setLastName(signUpRequest.getLastName());
        pendingUser.setHashedPassword(hashedPassword);
        pendingUser.setCreatedAt(System.currentTimeMillis());
        PendingUser user = pendingUserRepository.save(pendingUser);

        checkAndSendUserVerificationMail(user.getEmail());
    }

    @Override
    public ResponseCookie handleSignIn(String username, String password) {
        // check if user is awaiting to verified mail
        Optional<PendingUser> optionalPendingUser = pendingUserRepository.findByUsername(username);
        if ( optionalPendingUser.isPresent() ) {
            PendingUser pendingUser = optionalPendingUser.get();
            if ( PasswordUtil.verifyPassword(password, pendingUser.getHashedPassword()) ) {
                checkAndSendUserVerificationMail(pendingUser.getEmail());
                throw new APIException("email_not_verified");
            } else {
                throw new APIException("username_or_password_wrong");
            }
        }

        Optional<User> optionalUser = userRepository.findByUsername(username);
        if ( optionalUser.isEmpty() ) {
            throw new APIException("username_or_passwor");
        }

        User user = optionalUser.get();
        if ( !PasswordUtil.verifyPassword(password, user.getHashedPassword()) ) {
            throw new APIException("username_or_p");
        }

        Session session = sessionService.createSession(user);
        return sessionService.createCookie(session);
    }

    @Override
    public String handleAccountVerification(String email, String token) {
        Optional<PendingUser> optionalPendingUser = pendingUserRepository.findByEmail(email);
        if ( optionalPendingUser.isEmpty() )
            return "error_not_found";

        PendingUser pendingUser = optionalPendingUser.get();
        Optional<UserVerification> optionalUserVerification = userVerificationRepository.findByMail(email);

        if ( optionalUserVerification.isEmpty() )
            return "error_not_found";

        UserVerification userVerification = optionalUserVerification.get();
        if ( !userVerification.getToken().equals(token) )
            return "error_not_found";

        if ( isOlderThanMinutes(userVerification.getSentAt(), 15) ) {
            checkAndSendUserVerificationMail(pendingUser.getEmail());
            return "error_verification_timeout";
        }

        userService.createUser(
                pendingUser.getFirstName(),
                pendingUser.getLastName(),
                pendingUser.getEmail(),
                pendingUser.getUsername(),
                pendingUser.getHashedPassword()
        );
        pendingUserRepository.delete(pendingUser);
        LOGGER.info("User verified successfully mail: {}", pendingUser.getEmail());
        return "success_verification";
    }
}
