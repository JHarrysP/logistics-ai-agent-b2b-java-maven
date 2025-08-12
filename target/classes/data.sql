-- DATABASE INITIALIZATION SCRIPT
-- File: src/main/resources/data.sql
-- Product catalog data for the B2B Logistics System

-- Use MERGE statement for H2 database to avoid duplicates
MERGE INTO products (sku, name, category, weight, volume, stock_quantity, location) 
KEY(sku) VALUES 
('TILE-001', 'Ceramic Floor Tiles 60x60cm Premium White', 'TILES', 25.0, 0.50, 500, 'A-01-01'),
('TILE-002', 'Marble Wall Tiles 30x30cm Carrara Elegant', 'TILES', 15.0, 0.30, 300, 'A-01-02'),
('TILE-003', 'Porcelain Tiles Large Format 120x60cm', 'TILES', 35.0, 0.70, 200, 'A-01-03'),
('TILE-004', 'Mosaic Tiles Glass Premium Blue Mix', 'TILES', 8.0, 0.20, 150, 'A-01-04'),
('TILE-005', 'Natural Stone Tiles Travertine Rustic', 'TILES', 40.0, 0.60, 100, 'A-01-05'),
('TILE-006', 'Ceramic Wall Tiles 20x20cm Classic', 'TILES', 12.0, 0.25, 800, 'A-01-06'),
('TILE-007', 'Granite Floor Tiles 40x40cm Polished', 'TILES', 30.0, 0.40, 250, 'A-01-07'),
('TILE-008', 'Subway Metro Tiles 10x20cm White', 'TILES', 10.0, 0.15, 600, 'A-01-08');

-- PRODUCTS DATA - Construction Materials Category
INSERT INTO products (sku, name, category, weight, volume, stock_quantity, location) VALUES
  ('CONC-001', 'Portland Cement 50kg Bag Premium Grade',          'CONSTRUCTION_MATERIALS', 50.0, 0.40, 200, 'B-02-01'),
  ('CONC-002', 'Steel Rebar 12mm 6m Length Grade 60',            'CONSTRUCTION_MATERIALS',100.0, 0.80, 150, 'B-02-02'),
  ('CONC-003', 'Concrete Blocks Standard 20x20x40cm',            'CONSTRUCTION_MATERIALS', 75.0, 0.60, 300, 'B-02-03'),
  ('CONC-004', 'Aggregate Sand Fine Grade Washed',               'CONSTRUCTION_MATERIALS', 80.0, 0.70, 180, 'B-02-04'),
  ('CONC-005', 'Ready Mix Concrete M25 per m³',                  'CONSTRUCTION_MATERIALS',120.0, 1.00, 100, 'B-02-05'),
  ('CONC-006', 'Reinforcement Mesh 6mm 2x4m Sheets',             'CONSTRUCTION_MATERIALS', 45.0, 0.30, 120, 'B-02-06'),
  ('CONC-007', 'Structural Steel Beam H-Section 200mm',          'CONSTRUCTION_MATERIALS',250.0, 2.00,  80, 'B-02-07'),
  ('CONC-008', 'Precast Concrete Panels 2x1m',                   'CONSTRUCTION_MATERIALS',200.0, 1.50,  60, 'B-02-08');

-- PRODUCTS DATA - Roofing Materials Category
INSERT INTO products (sku, name, category, weight, volume, stock_quantity, location) VALUES
  ('ROOF-001', 'Clay Roof Tiles Red Traditional',                'ROOFING_MATERIALS', 40.0, 0.50, 300, 'C-03-01'),
  ('ROOF-002', 'Metal Roofing Sheets Galvanized 3x1m',           'ROOFING_MATERIALS', 25.0, 0.30, 200, 'C-03-02'),
  ('ROOF-003', 'Insulation Boards Polyurethane 10cm',            'ROOFING_MATERIALS', 15.0, 0.80, 150, 'C-03-03'),
  ('ROOF-004', 'Slate Roofing Tiles Premium Natural',            'ROOFING_MATERIALS', 50.0, 0.40, 100, 'C-03-04'),
  ('ROOF-005', 'Waterproof Membrane EPDM Rubber',                'ROOFING_MATERIALS', 30.0, 0.60,  80, 'C-03-05'),
  ('ROOF-006', 'Roof Truss Components Timber Frame',             'ROOFING_MATERIALS', 60.0, 1.20, 120, 'C-03-06'),
  ('ROOF-007', 'Guttering System PVC Complete Kit',              'ROOFING_MATERIALS', 20.0, 0.90,  90, 'C-03-07');

-- PRODUCTS DATA - Plumbing Supplies Category
INSERT INTO products (sku, name, category, weight, volume, stock_quantity, location) VALUES
  ('PLUMB-001', 'PVC Pipes 110mm Drainage 6m Length',            'PLUMBING_SUPPLIES', 12.0, 0.40, 250, 'D-04-01'),
  ('PLUMB-002', 'Copper Pipes 22mm Water Supply 3m',             'PLUMBING_SUPPLIES',  8.0, 0.20, 200, 'D-04-02'),
  ('PLUMB-003', 'Bathroom Fittings Premium Set Complete',        'PLUMBING_SUPPLIES', 30.0, 0.60, 100, 'D-04-03'),
  ('PLUMB-004', 'Water Tank Polyethylene 1000L',                 'PLUMBING_SUPPLIES', 50.0, 1.50,  50, 'D-04-04'),
  ('PLUMB-005', 'Pipe Fittings Assorted Pack Premium',           'PLUMBING_SUPPLIES',  5.0, 0.10, 300, 'D-04-05'),
  ('PLUMB-006', 'Drainage System Underground Complete',          'PLUMBING_SUPPLIES', 40.0, 1.00,  75, 'D-04-06'),
  ('PLUMB-007', 'Heat Pump Water Heater 300L Capacity',          'PLUMBING_SUPPLIES', 80.0, 1.80,  25, 'D-04-07');

-- SAMPLE ORDERS DATA - Must come before order_items
INSERT INTO orders (client_id, client_name, order_date, status, delivery_address, requested_delivery_date, total_weight, total_volume) VALUES
  ('CLIENT_HAMBURG_001', 'Hamburg Construction GmbH', '2025-08-01 09:00:00', 'PENDING', 'Baustelle Hafencity, Überseeallee 10, 20457 Hamburg, Germany', '2025-08-12 10:00:00', 2050.00, 37.50),
  ('CLIENT_HAMBURG_002', 'Hamburg Building Supplies Ltd', '2025-08-01 10:30:00', 'VALIDATED', 'Warehouse District, Speicherstadt 15, 20457 Hamburg, Germany', '2025-08-13 14:00:00', 2000.00, 28.00),
  ('CLIENT_BERLIN_001', 'Berlin Construction Services', '2025-08-01 11:15:00', 'IN_FULFILLMENT', 'Construction Site Potsdamer Platz, 10785 Berlin, Germany', '2025-08-14 09:00:00', 300.00, 21.60),
  ('CLIENT_MUNICH_001', 'Munich Building Materials', '2025-08-01 14:45:00', 'SCHEDULED', 'Industrial Zone, Landsberger Str. 400, 80687 Munich, Germany', '2025-08-15 11:30:00', 3750.00, 52.50);

-- SAMPLE ORDER ITEMS DATA - Now references existing orders
INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
  (
    (SELECT id FROM orders   WHERE client_id = 'CLIENT_HAMBURG_001'),
    (SELECT id FROM products WHERE sku       = 'TILE-001'),
    100,
    25.00
  ),
  (
    (SELECT id FROM orders   WHERE client_id = 'CLIENT_HAMBURG_001'),
    (SELECT id FROM products WHERE sku       = 'TILE-002'),
    50,
    15.50
  ),
  (
    (SELECT id FROM orders   WHERE client_id = 'CLIENT_HAMBURG_002'),
    (SELECT id FROM products WHERE sku       = 'CONC-001'),
    80,
    50.00
  ),
  (
    (SELECT id FROM orders   WHERE client_id = 'CLIENT_BERLIN_001'),
    (SELECT id FROM products WHERE sku       = 'ROOF-001'),
    120,
    40.00
  ),
  (
    (SELECT id FROM orders   WHERE client_id = 'CLIENT_MUNICH_001'),
    (SELECT id FROM products WHERE sku       = 'CONC-003'),
    150,
    75.00
  );

-- SAMPLE SHIPMENTS DATA - References existing orders
INSERT INTO shipments (order_id, truck_id, driver_id, status, scheduled_pickup, estimated_delivery, picking_instructions, requires_special_handling) VALUES
  (
    (SELECT id FROM orders WHERE client_id = 'CLIENT_HAMBURG_001'),
    'TRUCK-001',
    'DRIVER-001',
    'SCHEDULED',
    '2025-08-12 08:00:00',
    '2025-08-12 10:00:00',
    'Pick tiles from A-01 section. Handle with care - fragile items.',
    true
  ),
  (
    (SELECT id FROM orders WHERE client_id = 'CLIENT_HAMBURG_002'),
    'TRUCK-002',
    'DRIVER-002',
    'SCHEDULED',
    '2025-08-13 12:00:00',
    '2025-08-13 14:00:00',
    'Heavy concrete - use forklift. Load last due to weight.',
    true
  ),
  (
    (SELECT id FROM orders WHERE client_id = 'CLIENT_BERLIN_001'),
    'TRUCK-003',
    'DRIVER-003',
    'IN_TRANSIT',
    '2025-08-14 07:00:00',
    '2025-08-14 09:00:00',
    'Roof tiles from C-03 section. Stack carefully to prevent breakage.',
    true
  ),
  (
    (SELECT id FROM orders WHERE client_id = 'CLIENT_MUNICH_001'),
    'TRUCK-004',
    'DRIVER-004',
    'SCHEDULED',
    '2025-08-15 09:30:00',
    '2025-08-15 11:30:00',
    'Rapid set concrete - time sensitive delivery. Priority load.',
    true
  );
