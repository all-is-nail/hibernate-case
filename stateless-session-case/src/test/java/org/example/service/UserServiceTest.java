package org.example.service;

import org.example.config.AppConfig;
import org.example.entity.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AppConfig.class)
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Before
    public void setUp() {
        // No setup needed for H2 in-memory database
    }

    @Test
    public void testInitialData() {
        // 验证初始化脚本中的数据
        User alice = userService.getUserById(1L);
        assertNotNull(alice);
        assertEquals("Alice", alice.getName());
        assertEquals("alice@example.com", alice.getEmail());

        User bob = userService.getUserById(2L);
        assertNotNull(bob);
        assertEquals("Bob", bob.getName());
        assertEquals("bob@example.com", bob.getEmail());
    }

    @Test
    public void testCreateAndGetUser() {
        User user = new User();
        user.setName("John Doe");
        user.setEmail("john@example.com");

        userService.createUser(user);

        User fetchedUser = userService.getUserById(user.getId());
        assertNotNull(fetchedUser);
        assertEquals("John Doe", fetchedUser.getName());
        assertEquals("john@example.com", fetchedUser.getEmail());
    }
}
