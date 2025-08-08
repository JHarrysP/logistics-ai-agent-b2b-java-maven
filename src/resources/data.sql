-- DATABASE INITIALIZATION SCRIPT
-- File: src/main/resources/data.sql
-- Sample data for testing and demonstration of the B2B Logistics AI Agent

-- PRODUCTS DATA - Tiles Category
INSERT INTO products (sku, name, category, weight, volume, stock_quantity, location) VALUES
('TILE-001', 'Ceramic Floor Tiles 60x60cm Premium White', 'TILES', 25.0, 0.5, 500, 'A-01-01'),
('TILE-002', 'Marble Wall Tiles 30x30cm Carrara Elegant', 'TILES', 15.0, 0.3, 300, 'A-01-02'),
('TILE-003', 'Porcelain Tiles Large Format 120x60cm', 'TILES', 35.0, 0.7, 200, 'A-01-03'),
('TILE-004', 'Mosaic Tiles Glass Premium Blue Mix', 'TILES', 8.0, 0.2, 150, 'A-01-04'),
('TILE-005', 'Natural Stone Tiles Travertine Rustic', 'TILES', 40.0, 0.6, 100, 'A-01-05'),
('TILE-006', 'Ceramic Wall Tiles 20x20cm Classic', 'TILES', 12.0, 0.25, 800, 'A-01-06'),
('TILE-007', 'Granite Floor Tiles 40x40cm Polished', 'TILES', 30.0, 0.4, 250, 'A-01-07'),
('TILE-008', 'Subway Metro Tiles 10x20cm White', 'TILES', 10.0, 0.15, 600, 'A-01-08');

-- Construction Materials Category
INSERT INTO products (sku, name, category, weight, volume, stock_quantity, location) VALUES
('CONC-001', 'Portland Cement 50kg Bag Premium Grade', 'CONSTRUCTION_MATERIALS', 50.0, 0.4, 200, 'B-02-01'),
('CONC-002', 'Steel Rebar 12mm 6m Length Grade 60', 'CONSTRUCTION_MATERIALS', 100.0, 0.8, 150, 'B-02-02'),
('CONC-003', 'Concrete Blocks Standard 20x20x40cm', 'CONSTRUCTION_MATERIALS', 75.0, 0.6, 300, 'B-02-03'),
('CONC-004', 'Aggregate Sand Fine Grade Washed', 'CONSTRUCTION_MATERIALS', 80.0, 0.7, 180, 'B-02-04'),
('CONC-005', 'Ready Mix Concrete M25 per m³', 'CONSTRUCTION_MATERIALS', 120.0, 1.0, 100, 'B-02-05'),
('CONC-006', 'Reinforcement Mesh 6mm 2x4m Sheets', 'CONSTRUCTION_MATERIALS', 45.0, 0.3, 120, 'B-02-06'),
('CONC-007', 'Structural Steel Beam H-Section 200mm', 'CONSTRUCTION_MATERIALS', 250.0, 2.0, 80, 'B-02-07'),
('CONC-008', 'Precast Concrete Panels 2x1m', 'CONSTRUCTION_MATERIALS', 200.0, 1.5, 60, 'B-02-08');

-- Roofing Materials Category
INSERT INTO products (sku, name, category, weight, volume, stock_quantity, location) VALUES
('ROOF-001', 'Clay Roof Tiles Red Traditional', 'ROOFING_MATERIALS', 40.0, 0.5, 300, 'C-03-01'),
('ROOF-002', 'Metal Roofing Sheets Galvanized 3x1m', 'ROOFING_MATERIALS', 25.0, 0.3, 200, 'C-03-02'),
('ROOF-003', 'Insulation Boards Polyurethane 10cm', 'ROOFING_MATERIALS', 15.0, 0.8, 150, 'C-03-03'),
('ROOF-004', 'Slate Roofing Tiles Premium Natural', 'ROOFING_MATERIALS', 50.0, 0.4, 100, 'C-03-04'),
('ROOF-005', 'Waterproof Membrane EPDM Rubber', 'ROOFING_MATERIALS', 30.0, 0.6, 80, 'C-03-05'),
('ROOF-006', 'Roof Truss Components Timber Frame', 'ROOFING_MATERIALS', 60.0, 1.2, 120, 'C-03-06'),
('ROOF-007', 'Guttering System PVC Complete Kit', 'ROOFING_MATERIALS', 20.0, 0.9, 90, 'C-03-07');

-- Plumbing Supplies Category
INSERT INTO products (sku, name, category, weight, volume, stock_quantity, location) VALUES
('PLUMB-001', 'PVC Pipes 110mm Drainage 6m Length', 'PLUMBING_SUPPLIES', 12.0, 0.4, 250, 'D-04-01'),
('PLUMB-002', 'Copper Pipes 22mm Water Supply 3m', 'PLUMBING_SUPPLIES', 8.0, 0.2, 200, 'D-04-02'),
('PLUMB-003', 'Bathroom Fittings Premium Set Complete', 'PLUMBING_SUPPLIES', 30.0, 0.6, 100, 'D-04-03'),
('PLUMB-004', 'Water Tank Polyethylene 1000L', 'PLUMBING_SUPPLIES', 50.0, 1.5, 50, 'D-04-04'),
('PLUMB-005', 'Pipe Fittings Assorted Pack Premium', 'PLUMBING_SUPPLIES', 5.0, 0.1, 300, 'D-04-05'),
('PLUMB-006', 'Drainage System Underground Complete', 'PLUMBING_SUPPLIES', 40.0, 1.0, 75, 'D-04-06'),
('PLUMB-007', 'Heat Pump Water Heater 300L Capacity', 'PLUMBING_SUPPLIES', 80.0, 1.8, 25, 'D-04-07');

-- SAMPLE ORDERS DATA
INSERT INTO orders (client_id, client_name, order_date, status, delivery_address, requested_delivery_date, total_weight, total_volume) VALUES
('CLIENT_HAMBURG_001', 'Hamburg Construction GmbH', CURRENT_TIMESTAMP, 'RECEIVED', 'Baustelle Hafencity, Überseeallee 10, 20457 Hamburg, Germany', DATEADD('DAY', 3, CURRENT_TIMESTAMP), 0.0, 0.0),
('CLIENT_HAMBURG_002', 'Premium Tiles Hamburg', CURRENT_TIMESTAMP, 'RECEIVED', 'Showroom Eppendorf, Eppendorfer Weg 95, 20249 Hamburg, Germany', DATEADD('DAY', 2, CURRENT_TIMESTAMP), 0.0, 0.0),
('CLIENT_BERLIN_001', 'Berlin Building Supplies Ltd', DATEADD('HOUR', -2, CURRENT_TIMESTAMP), 'VALIDATED', 'Alexanderplatz Construction Site, 10178 Berlin, Germany', DATEADD('DAY', 4, CURRENT_TIMESTAMP), 0.0, 0.0),
('CLIENT_MUNICH_001', 'München Bau Materials', DATEADD('HOUR', -1, CURRENT_TIMESTAMP), 'INVENTORY_CHECKED', 'Marienplatz Renovation Project, 80331 München, Germany', DATEADD('DAY', 5, CURRENT_TIMESTAMP), 0.0, 0.0);

-- DATABASE INDEXES
CREATE INDEX IF NOT EXISTS idx_products_location ON products(location);
CREATE INDEX IF NOT EXISTS idx_products_category ON products(category);
CREATE INDEX IF NOT EXISTS idx_products_sku ON products(sku);
CREATE INDEX IF NOT EXISTS idx_products_stock_quantity ON products(stock_quantity);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_client_id ON orders(client_id);
CREATE INDEX IF NOT EXISTS idx_orders_order_date ON orders(order_date);
CREATE INDEX IF NOT EXISTS idx_orders_requested_delivery_date ON orders(requested_delivery_date);