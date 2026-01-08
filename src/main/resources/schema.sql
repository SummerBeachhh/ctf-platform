CREATE TABLE IF NOT EXISTS category (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) DEFAULT 'USER',
    score INT DEFAULT 0,
    is_vip BOOLEAN DEFAULT FALSE,
    oauth_provider VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS challenge (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    category_id INT,
    points INT DEFAULT 0,
    flag VARCHAR(255) NOT NULL,
    attachment_url VARCHAR(255),
    sort_order INT DEFAULT 0,
    is_vip BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (category_id) REFERENCES category(id)
);

CREATE TABLE IF NOT EXISTS submission (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    challenge_id INT,
    submission_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_correct BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (challenge_id) REFERENCES challenge(id)
);

CREATE TABLE IF NOT EXISTS orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    trade_no VARCHAR(64),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

INSERT INTO users (username, password, role, score, is_vip) VALUES 
('admin', 'admin123', 'ADMIN', 9999, true),
('user', 'user123', 'USER', 100, false),
('Cyber_Warrior', 'password', 'USER', 350, false),
('Root_User', 'password', 'USER', 200, false)
ON DUPLICATE KEY UPDATE 
password=VALUES(password), role=VALUES(role), score=VALUES(score), is_vip=VALUES(is_vip);

INSERT INTO category (name) VALUES ('Web'), ('Pwn'), ('Reverse'), ('Crypto'), ('Misc')
ON DUPLICATE KEY UPDATE name=VALUES(name);

INSERT INTO challenge (title, description, category_id, points, flag) 
SELECT '简单 Web', '在源代码中找到 Flag。', 1, 100, 'ctf{web_is_fun}'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM challenge WHERE title = '简单 Web');

INSERT INTO challenge (title, description, category_id, points, flag) 
SELECT 'SQL 注入', '绕过登录页面。(参考答案: admin'' -- )', 1, 200, 'ctf{sqli_master}'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM challenge WHERE title = 'SQL 注入');

INSERT INTO challenge (title, description, category_id, points, flag) 
SELECT '缓冲区溢出', '通过栈溢出获取 Flag。', 2, 300, 'ctf{stack_smashed}'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM challenge WHERE title = '缓冲区溢出');

INSERT INTO challenge (title, description, category_id, points, flag, sort_order, is_vip) 
SELECT '简单加密', '解密这段文字: Y3Rme2Jhc2U2NF9pc19lYXN5fQ==', 4, 150, 'ctf{base64_is_easy}', 80, false
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM challenge WHERE title = '简单加密');

INSERT INTO challenge (title, description, category_id, points, flag) 
SELECT '逆向工程', '反编译二进制文件并找到密钥。', 3, 250, 'ctf{reverse_engineering}'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM challenge WHERE title = '逆向工程');

INSERT INTO challenge (title, description, category_id, points, flag, sort_order, is_vip) 
SELECT 'Cookie 伪造', '管理员的 Cookie 是什么味道？', 1, 150, 'ctf{yummy_admin_cookie}', 70, true
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM challenge WHERE title = 'Cookie 伪造');

INSERT INTO challenge (title, description, category_id, points, flag, sort_order, is_vip) 
SELECT '机器人协议', '有些东西搜索引擎不应该看到。请检查 robots.txt。', 1, 50, 'ctf{robots_txt_is_public}', 90, false
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM challenge WHERE title = '机器人协议');

INSERT INTO challenge (title, description, category_id, points, flag) 
SELECT '凯撒密码', '加密信息：Fwza, Fwza, Fwza! 偏移量是关键。', 4, 100, 'ctf{caesar_cipher}'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM challenge WHERE title = '凯撒密码');

INSERT INTO challenge (title, description, category_id, points, flag, sort_order, is_vip) 
SELECT '签到题', '复制粘贴也是一种技术。Flag: ctf{welcome_to_ctf}', 5, 10, 'ctf{welcome_to_ctf}', 100, false
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM challenge WHERE title = '签到题');

INSERT INTO challenge (title, description, category_id, points, flag) 
SELECT 'Python 反序列化', '注意 pickle 的危险性。', 1, 400, 'ctf{pickle_rick_morty}'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM challenge WHERE title = 'Python 反序列化');
