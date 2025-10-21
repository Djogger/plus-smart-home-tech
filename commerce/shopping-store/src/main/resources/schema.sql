DROP TABLE IF EXISTS products;

CREATE TABLE IF NOT EXISTS products (
    product_id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    image_src VARCHAR(255),
    rating integer,
    price DOUBLE PRECISION,
    quantity_state VARCHAR(255) NOT NULL,
    product_state VARCHAR(255) NOT NULL,
    product_category VARCHAR(255) NOT NULL
);