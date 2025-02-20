-- Update stock_adjustment_type enum
DO $$ 
BEGIN 
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'stock_adjustment_type') THEN
        CREATE TYPE stock_adjustment_type AS ENUM (
            'MANUAL_ADJUSTMENT',
            'SALE',
            'PURCHASE',
            'INVENTORY_COUNT',
            'WASTE',
            'RETURN',
            'BATCH_UPDATE'
        );
    ELSE
        ALTER TYPE stock_adjustment_type ADD VALUE IF NOT EXISTS 'BATCH_UPDATE' AFTER 'RETURN';
    END IF;
END $$;

-- Add active column to menu_items if not exists
DO $$ 
BEGIN 
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'menu_items' AND column_name = 'active'
    ) THEN
        ALTER TABLE menu_items ADD COLUMN active boolean NOT NULL DEFAULT true;
    END IF;
END $$;

-- Add active column to menu_categories if not exists
DO $$ 
BEGIN 
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'menu_categories' AND column_name = 'active'
    ) THEN
        ALTER TABLE menu_categories ADD COLUMN active boolean NOT NULL DEFAULT true;
    END IF;
END $$;

-- Add restaurant_id to menu_categories if not exists
DO $$ 
BEGIN 
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'menu_categories' AND column_name = 'restaurant_id'
    ) THEN
        ALTER TABLE menu_categories ADD COLUMN restaurant_id bigint;
        ALTER TABLE menu_categories ADD CONSTRAINT fk_menu_categories_restaurant 
            FOREIGN KEY (restaurant_id) REFERENCES restaurants(id);
    END IF;
END $$;
