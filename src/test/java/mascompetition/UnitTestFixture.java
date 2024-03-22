package mascompetition;


import mascompetition.Entity.User;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Tests extend this fixture which provides default models
 */
public class UnitTestFixture {

    public static String HASHED_ADMIN_PASSWORD = "$2a$10$7hZKRE1GM3dLBw23sIl4qOqb6Ze8OijNGqzHFBs3kfdGQKHOJ4AeC";

    /**
     * Creates a default user for testing
     *
     * @return The builder to allow modifications before creation
     */
    public User.UserBuilder getUser() {

        return User.builder()
                .authorities(new ArrayList<>())
                .email("default@email.com")
                .id(UUID.randomUUID())
                .hashedPassword("Encoded Password");
    }

}
