package com.alaha.chala.service;

import com.alaha.chala.domain.UserAccount;
import com.alaha.chala.dto.UserAccountDto;
import com.alaha.chala.repository.UserAccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@DisplayName("Service/UserAccount")
@ExtendWith(MockitoExtension.class)
class UserAccountServiceTest {

    @InjectMocks private UserAccountService sut;

    @Mock private UserAccountRepository userAccountRepository;

    @DisplayName("If you search for an existing member ID, it returns member data as Optional.")
    @Test
    void givenExistentUsername_whenSearching_thenReturnsOptionalUserData() {
        // Given
        String username = "user";
        given(userAccountRepository.findById(username)).willReturn(Optional.of(createUserAccount(username)));

        // When
        Optional<UserAccountDto> result = sut.searchUser(username);

        // Then
        assertThat(result).isPresent();
        then(userAccountRepository).should().findById(username);
    }

    @DisplayName("If you search for a non-existent member ID, it returns an empty Optional.")
    @Test
    void givenNonexistentUsername_whenSearching_thenReturnsOptionalUserData() {
        // Given
        String username = "wrong-user";
        given(userAccountRepository.findById(username)).willReturn(Optional.empty());

        // When
        Optional<UserAccountDto> result = sut.searchUser(username);

        // Then
        assertThat(result).isEmpty();
        then(userAccountRepository).should().findById(username);
    }

    @DisplayName("If you input member information, it saves new member information to register and returns the corresponding member data.")
    @Test
    void givenUserParams_whenSaving_thenSavesUserAccount() {
        // Given
        UserAccount userAccount = createUserAccount("user");
        UserAccount savedUserAccount = createSigningUpUserAccount("user");
        given(userAccountRepository.save(userAccount)).willReturn(savedUserAccount);

        // When
        UserAccountDto result = sut.saveUser(
                userAccount.getUsername(),
                userAccount.getPassword(),
                userAccount.getEmail(),
                userAccount.getNickname(),
                userAccount.getMemo()
        );

        // Then
        assertThat(result)
                .hasFieldOrPropertyWithValue("username", userAccount.getUsername())
                .hasFieldOrPropertyWithValue("password", userAccount.getPassword())
                .hasFieldOrPropertyWithValue("email", userAccount.getEmail())
                .hasFieldOrPropertyWithValue("nickname", userAccount.getNickname())
                .hasFieldOrPropertyWithValue("memo", userAccount.getMemo())
                .hasFieldOrPropertyWithValue("createdBy", userAccount.getUsername())
                .hasFieldOrPropertyWithValue("modifiedBy", userAccount.getUsername());
        then(userAccountRepository).should().save(userAccount);
    }


    private UserAccount createUserAccount(String username) {
        return createUserAccount(username, null);
    }

    private UserAccount createSigningUpUserAccount(String username) {
        return createUserAccount(username, username);
    }

    private UserAccount createUserAccount(String username, String createdBy) {
        return UserAccount.of(
                username,
                "password",
                "e@mail.com",
                "nickname",
                "memo",
                createdBy
        );
    }

}
