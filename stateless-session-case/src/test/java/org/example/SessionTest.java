package org.example;

import org.example.config.AppConfig;
import org.example.entity.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AppConfig.class)
public class SessionTest {

    @Autowired
    private SessionFactory sessionFactory;

    /**
     * Tests session operations including saving a user, clearing the session,
     * and verifying persistence using Hibernate Session API.
     */
    @Test
    public void testFlushAndClearOperation() {
        // use sessionFactory to get session
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        User user = new User();
        user.setName("test");
        user.setEmail("aa@a.com");
        session.save(user);
        // after save, the session will contain the user
        session.flush();
        assert session.contains(user);
        // clear session
        session.clear();
        // after clear, the session will not contain the user
        assert !session.contains(user);
        // commit the transaction
        session.getTransaction().commit();
        // after commit, the user will be saved to the database
        assert user.equals(session.get(User.class, user.getId()));
        session.close();
    }
}
