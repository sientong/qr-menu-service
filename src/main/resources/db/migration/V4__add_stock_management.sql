-- Create stock adjustment type enum
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'stock_adjustment_type') THEN
        CREATE TYPE stock_adjustment_type AS ENUM (
            'MANUAL_ADJUSTMENT',
            'SALE',
            'PURCHASE',
            'INVENTORY_COUNT',
            'WASTE',
            'RETURN'
        );
    END IF;
END $$;

-- Add stock management columns to menu_items
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'menu_items' AND column_name = 'track_stock') THEN
        ALTER TABLE menu_items ADD COLUMN track_stock BOOLEAN NOT NULL DEFAULT false;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'menu_items' AND column_name = 'stock_quantity') THEN
        ALTER TABLE menu_items ADD COLUMN stock_quantity INTEGER;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'menu_items' AND column_name = 'low_stock_threshold') THEN
        ALTER TABLE menu_items ADD COLUMN low_stock_threshold INTEGER;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'menu_items' AND column_name = 'unit_cost') THEN
        ALTER TABLE menu_items ADD COLUMN unit_cost DECIMAL(10,2);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'menu_items' AND column_name = 'last_valuation_date') THEN
        ALTER TABLE menu_items ADD COLUMN last_valuation_date TIMESTAMP WITH TIME ZONE;
    END IF;
END $$;

-- Create stock history table
CREATE TABLE stock_history (
    id BIGSERIAL PRIMARY KEY,
    menu_item_id BIGINT NOT NULL,
    previous_quantity INTEGER NOT NULL,
    new_quantity INTEGER NOT NULL,
    adjustment_quantity INTEGER NOT NULL,
    adjustment_type stock_adjustment_type NOT NULL,
    adjusted_by VARCHAR(255) NOT NULL,
    adjusted_at TIMESTAMP WITH TIME ZONE NOT NULL,
    notes TEXT,
    CONSTRAINT fk_stock_history_menu_item FOREIGN KEY (menu_item_id) 
        REFERENCES menu_items(id) ON DELETE CASCADE
);

-- Create stock alerts table
CREATE TABLE stock_alerts (
    id BIGSERIAL PRIMARY KEY,
    menu_item_id BIGINT NOT NULL,
    alert_type VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    acknowledged_at TIMESTAMP WITH TIME ZONE,
    acknowledged_by VARCHAR(255),
    CONSTRAINT fk_stock_alerts_menu_item FOREIGN KEY (menu_item_id) 
        REFERENCES menu_items(id) ON DELETE CASCADE
);

-- Add indexes for better performance
CREATE INDEX idx_stock_history_menu_item ON stock_history(menu_item_id);
CREATE INDEX idx_stock_history_adjusted_at ON stock_history(adjusted_at);
CREATE INDEX idx_stock_alerts_menu_item ON stock_alerts(menu_item_id);
CREATE INDEX idx_stock_alerts_created_at ON stock_alerts(created_at);