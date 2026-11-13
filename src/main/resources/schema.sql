CREATE TABLE IF NOT EXISTS cliente(id SERIAL PRIMARY KEY, nombre TEXT NOT NULL, edad INT NOT NULL CHECK(edad>=0), embarazada BOOLEAN NOT NULL DEFAULT FALSE, puntos INT NOT NULL DEFAULT 0);
CREATE TABLE IF NOT EXISTS union_familiar(id SERIAL PRIMARY KEY, padre INT NOT NULL REFERENCES cliente(id), hijo INT NOT NULL REFERENCES cliente(id), CHECK(padre<>hijo));
CREATE TABLE IF NOT EXISTS producto(id SERIAL PRIMARY KEY, nombre TEXT UNIQUE NOT NULL, precio BIGINT NOT NULL CHECK(precio>=0));
CREATE TABLE IF NOT EXISTS venta(id SERIAL PRIMARY KEY, cliente_id INT REFERENCES cliente(id), subtotal BIGINT NOT NULL, descuento BIGINT NOT NULL, total BIGINT NOT NULL, puntos_ganados INT NOT NULL, fecha TIMESTAMP NOT NULL DEFAULT now());
CREATE TABLE IF NOT EXISTS venta_item(venta_id INT REFERENCES venta(id), producto_id INT REFERENCES producto(id), cantidad INT NOT NULL, PRIMARY KEY(venta_id,producto_id));
CREATE INDEX IF NOT EXISTS idx_venta_fecha ON venta(fecha);
CREATE INDEX IF NOT EXISTS idx_union_padre ON union_familiar(padre);
INSERT INTO producto(nombre,precio) VALUES('Manzana',1500),('Leche',4200),('Pan',3000),('Arroz 1 kg',5200),('Huevos x12',12500),('Café',14000),('Aceite',11000),('Jabón',3800) ON CONFLICT DO NOTHING;
INSERT INTO cliente(nombre,edad,embarazada,puntos) SELECT * FROM (VALUES('Ana Gómez',34,false,12),('Carlos Gómez',67,false,9),('Laura Ruiz',29,true,0),('Pedro Ruiz',41,false,0),('Marta Soto',72,false,0)) v WHERE NOT EXISTS(SELECT 1 FROM cliente);
