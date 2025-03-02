-- 创建用户表
CREATE TABLE IF NOT EXISTS User (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL
);

-- 插入初始数据
INSERT INTO User (name, email) VALUES ('Alice', 'alice@example.com');
INSERT INTO User (name, email) VALUES ('Bob', 'bob@example.com');