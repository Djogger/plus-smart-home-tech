DROP TABLE IF EXISTS shopping_cart_items;
DROP TABLE IF EXISTS shopping_cart;
DROP TABLE IF EXISTS warehouse_product;

CREATE TABLE IF NOT EXISTS warehouse_product (
    product_id UUID PRIMARY KEY,
    quantity INTEGER,
    fragile BOOLEAN,
    width DOUBLE PRECISION,
    height DOUBLE PRECISION,
    depth DOUBLE PRECISION,
    weight DOUBLE PRECISION
);

CREATE TABLE IF NOT EXISTS bookings (
    shopping_cart_id UUID PRIMARY KEY,
    delivery_weight DOUBLE PRECISION NOT NULL,
    delivery_volume DOUBLE PRECISION NOT NULL,
    fragile BOOLEAN NOT NULL,
    order_id UUID
);

CREATE TABLE IF NOT EXISTS booking_products (
    shopping_cart_id UUID REFERENCES bookings(shopping_cart_id) ON DELETE CASCADE PRIMARY KEY,
    product_id UUID NOT NULL,
    quantity INTEGER
);
