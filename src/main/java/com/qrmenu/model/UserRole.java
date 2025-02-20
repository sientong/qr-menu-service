package com.qrmenu.model;

/**
 * Represents the different roles a user can have in the system.
 */
public enum UserRole {
    /**
     * Super administrator with access to all restaurants and system-wide operations
     */
    SUPER_ADMIN,
    
    /**
     * Restaurant administrator with full access to their restaurant
     */
    RESTAURANT_ADMIN,
    
    /**
     * Restaurant manager with limited administrative access
     */
    RESTAURANT_MANAGER,
    
    /**
     * Waiter with access to order management
     */
    WAITER,
    
    /**
     * Customer with read-only access to menu
     */
    CUSTOMER
}