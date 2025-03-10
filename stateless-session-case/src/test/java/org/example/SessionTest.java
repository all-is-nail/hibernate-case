package org.example;

import java.util.List;

import org.example.config.AppConfig;
import org.example.entity.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AppConfig.class)
public class SessionTest {

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private TransactionTemplate transactionTemplate;

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
        session.flush();

        assertTrue("Session should contain user after flush", session.contains(user));

        session.clear();
        assertFalse("Session should not contain user after clear", session.contains(user));

        session.getTransaction().commit();
        assertEquals("User should be retrievable from database after commit",
                user,
                session.get(User.class, user.getId()));

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
        firstSession.flush();

        Session secondSession = sessionFactory.openSession();
        secondSession.beginTransaction();

        User notExistsUser = secondSession.get(User.class, user.getId());
        assertNull("User should not be visible in second session before commit", notExistsUser);

        firstSession.getTransaction().commit();
        firstSession.close();

        User existsUser = secondSession.get(User.class, user.getId());
        assertNotNull("User should be visible in second session after commit", existsUser);
        assertEquals("Retrieved user should match original user", user, existsUser);

        secondSession.close();
    }

    /**
     * Tests bulk insert operation using Hibernate Session API.
     * 
     * This test performs a bulk insert of 100 users into the database.
     * It uses a session to save the users and flushes the session after every 10 users.
     * The session is also cleared after every flush to ensure the users are not visible in the session.
     */
    @Test
    public void testBulkInsert() {
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        int count = 0;
        for (int i = 0; i < 100; i++) {
            User user = new User();
            user.setName("testBulkInsert" + i);
            user.setEmail("aa@a.com");
            session.save(user);
            if (++count % 10 == 0) {
                flushAndClearSession(session);
                count = 0;
            }
        }

        if (count > 0) {
            flushAndClearSession(session);
        }

        session.getTransaction().commit();
        session.close();

        try (Session newSession = sessionFactory.openSession()) {
            List<User> users = newSession.createQuery("from User where name like 'testBulkInsert%'", User.class).list();
            assertEquals("Should have inserted 100 users", 100, users.size());
            for (int i = 0; i < 100; i++) {
                User user = users.get(i);
                assertEquals("testBulkInsert" + i, user.getName());
                assertEquals("aa@a.com", user.getEmail());
            }
        }
    }

    /**
     * Tests bulk insert operation using TransactionTemplate.
     * 
     * This test performs a bulk insert of 100 users into the database.
     * It uses a TransactionTemplate to execute the insert operation.
     * The session is also flushed and cleared after every 10 users.
     */
    @Test
    public void testBulkInsertWithTransactionTemplate() {
        transactionTemplate.execute(status -> {
            Session session = sessionFactory.getCurrentSession();

            int count = 0;
            for (int i = 0; i < 100; i++) {
                User user = new User();
                user.setName("testBulkInsertTx" + i);
                user.setEmail("aa@a.com");
                session.save(user);
                if (++count % 10 == 0) {
                    flushAndClearSession(session);
                    count = 0;
                }
            }

            if (count > 0) {
                flushAndClearSession(session);
            }

            return null;
        });

        try (Session verifySession = sessionFactory.openSession()) {
            List<User> users = verifySession.createQuery("from User where name like 'testBulkInsertTx%'", User.class).list();
            assertEquals("Should have inserted 100 users", 100, users.size());

            for (int i = 0; i < 100; i++) {
                User user = users.get(i);
                assertEquals("testBulkInsertTx" + i, user.getName());
                assertEquals("aa@a.com", user.getEmail());
            }
        }
    }

    /**
     * Flushes and clears the session.
     * 
     * This method flushes the session and clears it to ensure the users are not visible in the session.
     * 
     * @param session The session to flush and clear.
     */
    private void flushAndClearSession(Session session) {
        session.flush();
        session.clear();
    }

}
