package org.example;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.example.config.AppConfig;
import org.example.entity.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import java.util.Properties;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = BatchProcessingTest.BatchTestConfig.class)
public class BatchProcessingTest {

    @Autowired
    private SessionFactory sessionFactory;

    @Before
    public void clearData() {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.createQuery("DELETE FROM User WHERE name LIKE 'batch%'").executeUpdate();
            session.getTransaction().commit();
        }
    }

    @Test
    public void testBatchInsert() {
        int batchSize = 20;
        int totalRecords = 100;

        // Using batch insert
        long startTime = System.currentTimeMillis();
        
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            
            for (int i = 0; i < totalRecords; i++) {
                User user = new User();
                user.setName("batch_insert_" + i);
                user.setEmail("batch" + i + "@example.com");
                session.save(user);
                
                // Flush and clear session at each batch interval
                if (i > 0 && i % batchSize == 0) {
                    session.flush();
                    session.clear();
                }
            }
            
            session.getTransaction().commit();
        }
        
        long endTime = System.currentTimeMillis();
        System.out.println("Batch insert of " + totalRecords + " records took: " + (endTime - startTime) + "ms");

        // Verify the result
        try (Session session = sessionFactory.openSession()) {
            List<User> users = session.createQuery("FROM User WHERE name LIKE 'batch_insert_%'", User.class).list();
            assertEquals(totalRecords, users.size());
        }
    }

    @Test
    public void testBatchUpdate() {
        int batchSize = 20;
        int totalRecords = 100;
        
        // Insert test data first
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            
            for (int i = 0; i < totalRecords; i++) {
                User user = new User();
                user.setName("batch_update_" + i);
                user.setEmail("original" + i + "@example.com");
                session.save(user);
            }
            
            session.getTransaction().commit();
        }
        
        // Using batch update
        long startTime = System.currentTimeMillis();
        
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            
            List<User> usersToUpdate = session.createQuery("FROM User WHERE name LIKE 'batch_update_%'", User.class).list();
            
            int count = 0;
            for (User user : usersToUpdate) {
                user.setEmail("updated" + count + "@example.com");
                session.update(user);
                
                count++;
                // Flush and clear session at each batch interval
                if (count % batchSize == 0) {
                    session.flush();
                    session.clear();
                }
            }
            
            session.getTransaction().commit();
        }
        
        long endTime = System.currentTimeMillis();
        System.out.println("Batch update of " + totalRecords + " records took: " + (endTime - startTime) + "ms");

        // Verify the result
        try (Session session = sessionFactory.openSession()) {
            List<User> users = session.createQuery("FROM User WHERE email LIKE 'updated%'", User.class).list();
            assertEquals(totalRecords, users.size());
        }
    }
    
    @Test
    public void compareNonBatchVsBatchInsert() {
        int totalRecords = 100;
        
        // Non-batch insert (committing after each insert)
        long startTimeNonBatch = System.currentTimeMillis();
        
        for (int i = 0; i < totalRecords; i++) {
            try (Session session = sessionFactory.openSession()) {
                session.beginTransaction();
                
                User user = new User();
                user.setName("batch_compare_non_batch_" + i);
                user.setEmail("nonbatch" + i + "@example.com");
                session.save(user);
                
                session.getTransaction().commit();
            }
        }
        
        long endTimeNonBatch = System.currentTimeMillis();
        long nonBatchTime = endTimeNonBatch - startTimeNonBatch;
        System.out.println("Non-batch insert of " + totalRecords + " records took: " + nonBatchTime + "ms");

        // Batch insert
        int batchSize = 20;
        long startTimeBatch = System.currentTimeMillis();
        
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            
            for (int i = 0; i < totalRecords; i++) {
                User user = new User();
                user.setName("batch_compare_batch_" + i);
                user.setEmail("batch" + i + "@example.com");
                session.save(user);
                
                // Flush and clear session at each batch interval
                if (i > 0 && i % batchSize == 0) {
                    session.flush();
                    session.clear();
                }
            }
            
            session.getTransaction().commit();
        }
        
        long endTimeBatch = System.currentTimeMillis();
        long batchTime = endTimeBatch - startTimeBatch;
        System.out.println("Batch insert of " + totalRecords + " records took: " + batchTime + "ms");
        System.out.println("Performance improvement: " + (nonBatchTime / (double)batchTime) + "x faster with batching");
    }

    /**
     * Configuration class specifically for batch processing tests
     */
    @Configuration
    @Import(AppConfig.class)
    public static class BatchTestConfig {
        
        @Bean
        public Properties hibernateProperties() {
            Properties properties = new Properties();
            properties.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
            properties.setProperty("hibernate.show_sql", "false");
            properties.setProperty("hibernate.format_sql", "true");
            properties.setProperty("hibernate.hbm2ddl.auto", "none");
            
            // Important settings for batch processing
            properties.setProperty("hibernate.jdbc.batch_size", "20");
            properties.setProperty("hibernate.order_inserts", "true");
            properties.setProperty("hibernate.order_updates", "true");
            properties.setProperty("hibernate.jdbc.batch_versioned_data", "true");
            
            return properties;
        }
    }
} 