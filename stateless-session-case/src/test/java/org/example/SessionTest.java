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

    /**
     * Tests next scenario:
     * 1. Open the first session
     * 2. Begin a transaction for the first session
     * 3. Create a user
     * 4. Save the user
     * 5. Flush the session but not commit
     * 6. Open the second session
     * 7. Get the user from the database
     * 8. Verify the user is null
     * 9. Commit the transaction for the first session
     * 10. Close the first session
     * 11. use the second session to get the user from the database
     * 12. Verify the user is not null
     * 13. Close the second session
     */
    @Test
    public void testFlushAndCommitOperation() {
        // use sessionFactory to get firstSession
        Session firstSession = sessionFactory.openSession();
        firstSession.beginTransaction();
        User user = new User();
        user.setName("test");
        user.setEmail("test@test.org");
        firstSession.save(user);
        // flush the session but not commit
        firstSession.flush();
        // open a new session
        Session secondSession = sessionFactory.openSession();
        secondSession.beginTransaction();
        // get the user from the database
        User notExistsUser = secondSession.get(User.class, user.getId());
        assert notExistsUser == null;
        // commit the transaction
        firstSession.getTransaction().commit();
        // close the first session
        firstSession.close();
        // use the second session to get the user from the database
        User existsUser = secondSession.get(User.class, user.getId());
        assert existsUser != null;
        assert user.equals(existsUser);
        // close the second session
        secondSession.close();
    }
}
