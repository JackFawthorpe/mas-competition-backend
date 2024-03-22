package mascompetition.integration;

import org.junit.jupiter.api.Test;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthenticationTest extends IntegrationTestFixture {

    @Test
    void login_bluesky_302() throws Exception {

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("email", "default@email.com");
        formData.add("password", "admin");

        when(userRepository.findByEmail("default@email.com")).thenReturn(getUser().hashedPassword(HASHED_ADMIN_PASSWORD).build());

        mockMvc.perform(post("/api/v1/login")
                        .params(formData))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void login_wrongPassword_302() throws Exception {

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("email", "default@email.com");
        formData.add("password", "wrong_password");

        when(userRepository.findByEmail("default@email.com")).thenReturn(getUser().hashedPassword(HASHED_ADMIN_PASSWORD).build());

        mockMvc.perform(post("/api/v1/login")
                        .params(formData))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/api/v1/login"));
    }

    @Test
    void login_noPassword_302() throws Exception {

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("email", "default@email.com");

        when(userRepository.findByEmail("default@email.com")).thenReturn(getUser().hashedPassword(HASHED_ADMIN_PASSWORD).build());

        mockMvc.perform(post("/api/v1/login")
                        .params(formData))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/api/v1/login"));
    }

    @Test
    void login_incorrectEmail_302() throws Exception {

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("email", "@email.com");
        formData.add("password", "wrong_password");

        when(userRepository.findByEmail("default@email.com")).thenReturn(getUser().hashedPassword(HASHED_ADMIN_PASSWORD).build());

        mockMvc.perform(post("/api/v1/login")
                        .params(formData))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/api/v1/login"));
    }

    @Test
    void login_noEmail_302() throws Exception {

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("password", "wrong_password");

        when(userRepository.findByEmail("default@email.com")).thenReturn(getUser().hashedPassword(HASHED_ADMIN_PASSWORD).build());

        mockMvc.perform(post("/api/v1/login")
                        .params(formData))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/api/v1/login"));
    }

    @Test
    void logout_bluesky_302() throws Exception {

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("email", "default@email.com");
        formData.add("password", "admin");

        when(userRepository.findByEmail("default@email.com")).thenReturn(getUser().hashedPassword(HASHED_ADMIN_PASSWORD).build());

        mockMvc.perform(post("/api/v1/login")
                        .params(formData))
                .andReturn();

        mockMvc.perform(post("/api/v1/logout"))
                .andExpect(redirectedUrl("/api/v1/login"));
    }
}
