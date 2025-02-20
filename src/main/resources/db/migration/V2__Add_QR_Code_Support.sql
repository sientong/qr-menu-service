-- Create restaurant_tables table
CREATE TABLE IF NOT EXISTS restaurant_tables (
    id BIGSERIAL PRIMARY KEY,
    restaurant_id BIGINT NOT NULL REFERENCES restaurants(id),
    table_number VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    capacity INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP ,
    UNIQUE (restaurant_id, table_number)
);

-- Create qr_codes table
CREATE TABLE IF NOT EXISTS qr_codes (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    restaurant_id BIGINT NOT NULL REFERENCES restaurants(id),
    table_id BIGINT REFERENCES restaurant_tables(id),
    expires_at TIMESTAMP NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP 
);

-- Update existing QR codes to have active status
UPDATE qr_codes SET active = true WHERE active IS NULL;

-- Make table_id not null after updating existing records
ALTER TABLE qr_codes
ALTER COLUMN table_id SET NOT NULL; 