DROP TABLE IF EXISTS shopping_cart_items;
DROP TABLE IF EXISTS shopping_cart;

CREATE TABLE IF NOT EXISTS shopping_cart (
    shopping_cart_id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    cart_state VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS shopping_cart_items (
    product_id uuid NOT NULL,
    quantity INTEGER,
    cart_id uuid REFERENCES shopping_cart (shopping_cart_id) ON DELETE CASCADE
)
