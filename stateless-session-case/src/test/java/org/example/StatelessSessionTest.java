package org.example;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.example.config.AppConfig;
import org.example.entity.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.support.TransactionTemplate;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AppConfig.class)
public class StatelessSessionTest {

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    public void testStatelessSession() {
        StatelessSession statelessSession = sessionFactory.openStatelessSession();
        statelessSession.beginTransaction();

        User user = new User();
        user.setName("test");
        user.setEmail("aa@a.com");
        statelessSession.insert(user);
        statelessSession.getTransaction().commit();

        statelessSession.close();
    }

    @Test
    public void testStatelessSessionWithBulkInsert() {
        transactionTemplate.execute(status -> {
            StatelessSession statelessSession = sessionFactory.openStatelessSession();

            try {
                for (int i = 0; i < 10; i++) {
                    User user = new User();
                    user.setName("testStatelessSession" + i);
                    user.setEmail("aa@a.com");
                    statelessSession.insert(user);
                }

                return null;
            } finally {
                statelessSession.close();
            }
        });

        try (Session session = sessionFactory.openSession()) {
            List<User> users = session.createQuery("from User where name like 'testStatelessSession%'", User.class).list();
            assertEquals("Should have inserted 10 users", 10, users.size());
            for (int i = 0; i < 10; i++) {
                User user = users.get(i);
                assertEquals("testStatelessSession" + i, user.getName());
                assertEquals("aa@a.com", user.getEmail());
            }
        }
    }
    
}
