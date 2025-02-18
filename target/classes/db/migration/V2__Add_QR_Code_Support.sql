-- Create restaurant_tables table
CREATE TABLE restaurant_tables (
    id BIGSERIAL PRIMARY KEY,
    restaurant_id BIGINT NOT NULL REFERENCES restaurants(id),
    table_number VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    capacity INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    UNIQUE (restaurant_id, table_number)
);

-- Create qr_codes table
CREATE TABLE qr_codes (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    restaurant_id BIGINT NOT NULL REFERENCES restaurants(id),
    table_id BIGINT REFERENCES restaurant_tables(id),
    expires_at TIMESTAMP NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL
);

-- Add image support to menu items
ALTER TABLE menu_items
ADD COLUMN image_url VARCHAR(255);

-- Update existing QR codes to have active status
UPDATE qr_codes SET active = true WHERE active IS NULL;

-- Make table_id not null after updating existing records
ALTER TABLE qr_codes
ALTER COLUMN table_id SET NOT NULL; 