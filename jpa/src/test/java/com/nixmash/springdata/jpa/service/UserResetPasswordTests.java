package com.nixmash.springdata.jpa.service;

import com.nixmash.springdata.jpa.config.ApplicationConfig;
import com.nixmash.springdata.jpa.dto.UserPasswordDTO;
import com.nixmash.springdata.jpa.enums.DataConfigProfile;
import com.nixmash.springdata.jpa.enums.ResetPasswordResult;
import com.nixmash.springdata.jpa.model.User;
import com.nixmash.springdata.jpa.model.UserToken;
import com.nixmash.springdata.jpa.repository.UserTokenRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ApplicationConfig.class)
@Transactional
@ActiveProfiles(DataConfigProfile.H2)
public class UserResetPasswordTests {

    @Autowired
    UserService userService;

    @Autowired
    UserTokenRepository userTokenRepository;

    private User erwin;

    @Before
    public void setup() {
        erwin = userService.getUserByUsername("erwin");
        userTokenRepository.deleteAll();
    }

    @Test
    public void createUserToken_RetrievesUser() {
        UserToken userToken = userService.createUserToken(erwin);
        Optional<UserToken> found = userService.getUserToken(userToken.getToken());
        if (found.isPresent())
            assertEquals(found.get().getUser(), erwin);
    }

    @Test
    public void update_Forgot_PasswordReturns_FORGOT_SUCCESSFUL() {
        assertEquals(userService.updatePassword(erwinPasswordDTO(-1L)), ResetPasswordResult.FORGOT_SUCCESSFUL);
    }

    @Test
    public void existingUserTokensAreUpdated() {
        UserToken userToken = userService.createUserToken(erwin);
        String token = userToken.getToken();
        userToken = userService.createUserToken(erwin);
        String newToken = userToken.getToken();
        assertNotEquals(token, newToken);
        assertFalse(userService.getUserToken(token).isPresent());
        assertTrue(userService.getUserToken(newToken).isPresent());
    }

    @Test
    public void InitializedOptionalEmptyIsNotPresent() {
        Optional<UserToken> userToken = Optional.empty();
        assertFalse(userToken.isPresent());
    }

    // region Token Expiration tests

    @Test
    public void expiredTimeTests() {
        final Calendar cal = Calendar.getInstance();
        assertFalse(isValidToken(ONEMINUTEAFTER));
        assertTrue(isValidToken(ONEMINUTEBEFORE));

        UserToken userToken = userService.createUserToken(erwin);
        assertTrue(isValidToken((int) userToken.getTokenExpiration().getTime()));
    }

    private static final int ONEMINUTEAFTER = -1;
    private static final int ONEMINUTEBEFORE = 1;

    public Boolean isValidToken(int expiration) {
        boolean isValidToken = false;
        final Calendar cal = Calendar.getInstance();
        if (startTimestamp(expiration).getTime() - cal.getTime().getTime() > 0) {
            isValidToken = true;
        }
        return isValidToken;
    }

    private Timestamp startTimestamp(int expiration) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(new Date().getTime());
        cal.add(Calendar.MINUTE, expiration);
        return new Timestamp(cal.getTime().getTime());
    }

    // endregion

    @Test
    public void update_Reset_PasswordReturns_LOGGEDIN_SUCCESSFUL() {
        assertEquals(userService.updatePassword(erwinPasswordDTO(erwin.getId())), ResetPasswordResult.LOGGEDIN_SUCCESSFUL);
    }

    @Test
    public void update_BadToken_PasswordReturns_ERROR() {
        // token will not be found in user_tokens
        UserPasswordDTO userPasswordDTO = new UserPasswordDTO(-1, UUID.randomUUID().toString(), "password", "password");
        assertEquals(userService.updatePassword(userPasswordDTO), ResetPasswordResult.ERROR);
    }

    private UserPasswordDTO erwinPasswordDTO(long userId) {
        UserToken erwinToken = userService.createUserToken(erwin);
        return new UserPasswordDTO(userId, erwinToken.getToken(), "password", "password");
    }

}
