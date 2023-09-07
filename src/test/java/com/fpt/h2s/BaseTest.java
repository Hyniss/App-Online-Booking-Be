package com.fpt.h2s;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fpt.h2s.interceptors.models.MessageResolver;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.repositories.UserRepository;
import com.fpt.h2s.utilities.Mappers;
import com.fpt.h2s.utilities.MoreStrings;
import com.github.javafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class BaseTest {

    static {
        H2sApplication.defineSystemEnv();
    }

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected MessageResolver messageResolver;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;
    protected static final Faker FAKER = new Faker();

    protected static <T> T responseOf(final MvcResult mvcResult, final Class<T> clazz) throws UnsupportedEncodingException {
        final String responseBody = mvcResult.getResponse().getContentAsString();
        final ApiResponse<Object> responseDto = Mappers.mapToObjectFrom(responseBody, ApiResponse.class);
        assert responseDto != null;
        final Map<String, Object> dataAsMap = (Map<String, Object>) responseDto.getData();
        return Mappers.fromMap(dataAsMap, clazz);
    }

    protected static <T> T responseOf(final MvcResult mvcResult, final TypeReference<T> type) throws UnsupportedEncodingException {
        final ApiResponse<HashMap<String, Object>> responseDto = convertFromJsonResponseToApiResponse(mvcResult);
        final Map<String, Object> dataAsMap = Optional.of((Map<String, Object>) responseDto.getData()).orElse(Map.of());
        return Mappers.fromMap(dataAsMap, type);
    }
    private static ApiResponse<HashMap<String, Object>> convertFromJsonResponseToApiResponse(final MvcResult mvcResult) throws UnsupportedEncodingException {
        final String responseBody = mvcResult.getResponse().getContentAsString();
        return Mappers.mapToObjectFrom(responseBody, new TypeReference<>() {
        });
    }

    protected static String messageOf(final MvcResult mvcResult) throws UnsupportedEncodingException {
        return convertFromJsonResponseToApiResponse(mvcResult).getMessage();
    }

    protected User createUser(final Function<User.UserBuilder, User.UserBuilder> function) {
        final User.UserBuilder<?, ?> builder = User.builder()
            .status(User.Status.ACTIVE)
            .email(MoreStrings.randomStringWithLength(16) + "@gmail.com")
            .username(MoreStrings.randomStringWithLength(16))
            .roles(User.Role.BUSINESS_OWNER.name())
            .password("Hi111111111111")
            .createdAt(Timestamp.from(Instant.now()))
            .updatedAt(Timestamp.from(Instant.now()));
        final User user = function.apply(builder).build();
        final User userToCreate = user.withPassword(this.passwordEncoder.encode(user.getPassword()));

        return this.userRepository.save(userToCreate);
    }

    protected User createUser() {
        return this.createUser(u -> u);
    }

}
