ALTER TABLE users
ADD COLUMN restaurant_id BIGINT,
ADD CONSTRAINT fk_users_restaurant FOREIGN KEY (restaurant_id) 
    REFERENCES restaurants(id) ON DELETE SET NULL;

CREATE INDEX idx_users_restaurant ON users(restaurant_id); 