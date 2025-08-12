import React, { useState, useEffect, ChangeEvent } from 'react';
import { createTheme, ThemeProvider } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import {
  AppBar, Toolbar, Typography, Container, Grid, Card, CardContent,
  Button, TextField, Select, MenuItem, FormControl, InputLabel,
  Box, Chip, Alert, Snackbar, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, LinearProgress, Switch,
  FormControlLabel, Dialog, DialogTitle, DialogContent, DialogActions,
  IconButton, Paper
} from '@mui/material';
import {
  Dashboard, Send, Search, Refresh, Close
} from '@mui/icons-material';
import axios, { AxiosError } from 'axios';

const API_BASE = 'http://localhost:8080/api';

// TypeScript interfaces
interface Stats {
  totalOrders: number;
  receivedOrders: number;
  inTransitOrders: number;
  deliveredOrders: number;
  cancelledOrders: number;
}

interface Order {
  orderId: number;
  clientName: string;
  status: string;
  totalItems?: number;
  totalWeight?: number;
  orderDate: string;
  estimatedDelivery?: string;
  shipmentInfo?: {
    shipmentId: number;
    truckId: string;
  };
}

interface OrderForm {
  clientId: string;
  clientName: string;
  deliveryAddress: string;
  deliveryDate: string;
  productSku: string;
  quantity: number;
  unitPrice: number;
}

interface SnackbarState {
  open: boolean;
  message: string;
  severity: 'success' | 'error' | 'warning' | 'info';
}

const theme = createTheme({
  palette: {
    mode: 'light',
    primary: { main: '#1976d2' },
    secondary: { main: '#dc004e' },
    background: { default: '#f5f5f5' },
  },
  typography: {
    fontFamily: '"Roboto", "Helvetica", "Arial", sans-serif',
    h4: { fontWeight: 600 },
    h5: { fontWeight: 600 },
    h6: { fontWeight: 500 },
  },
});

function App(): JSX.Element {
  const [stats, setStats] = useState<Stats>({
    totalOrders: 0,
    receivedOrders: 0,
    inTransitOrders: 0,
    deliveredOrders: 0,
    cancelledOrders: 0
  });
  const [orders, setOrders] = useState<Order[]>([]);
  const [aiActive, setAiActive] = useState<boolean>(true);
  const [autoRefresh, setAutoRefresh] = useState<boolean>(false);
  const [snackbar, setSnackbar] = useState<SnackbarState>({
    open: false,
    message: '',
    severity: 'success'
  });
  const [loading, setLoading] = useState<boolean>(false);
  const [orderForm, setOrderForm] = useState<OrderForm>({
    clientId: 'CLIENT_DASHBOARD_001',
    clientName: 'Dashboard Test Client',
    deliveryAddress: 'Hamburg Business District, Neuer Wall 50, 20354 Hamburg, Germany',
    deliveryDate: new Date(Date.now() + 3 * 24 * 60 * 60 * 1000).toISOString().slice(0, 16),
    productSku: 'TILE-001',
    quantity: 25,
    unitPrice: 25.00
  });
  const [statusSearch, setStatusSearch] = useState<string>('');
  const [shipmentId, setShipmentId] = useState<string>('');
  const [orderDetails, setOrderDetails] = useState<Order | null>(null);
  const [detailsDialog, setDetailsDialog] = useState<boolean>(false);

  useEffect(() => {
    loadStats();
    loadOrders();
  }, []);

  useEffect(() => {
    let interval: NodeJS.Timeout;
    if (autoRefresh) {
      interval = setInterval(() => {
        loadStats();
        loadOrders();
      }, 10000);
    }
    return () => {
      if (interval) clearInterval(interval);
    };
  }, [autoRefresh]);

  const showSnackbar = (message: string, severity: 'success' | 'error' | 'warning' | 'info' = 'success'): void => {
    setSnackbar({ open: true, message, severity });
  };

  const loadStats = async (): Promise<void> => {
    try {
      const response = await axios.get<Stats>(`${API_BASE}/orders/stats`);
      setStats(response.data);
    } catch (error) {
      console.error('Error loading stats:', error);
      showSnackbar('Error loading statistics', 'error');
    }
  };

  const loadOrders = async (): Promise<void> => {
    setLoading(true);
    try {
      const statuses = ['RECEIVED', 'VALIDATED', 'FULFILLED', 'IN_TRANSIT', 'DELIVERED'];
      let allOrders: Order[] = [];

      for (const status of statuses) {
        try {
          const response = await axios.get<Order[]>(`${API_BASE}/orders/status/${status}`);
          allOrders = [...allOrders, ...response.data];
        } catch (e) {
          // Ignore missing statuses
        }
      }

      setOrders(allOrders.sort((a, b) => new Date(b.orderDate).getTime() - new Date(a.orderDate).getTime()));
    } catch (error) {
      const err = error as AxiosError;
      showSnackbar('Error loading orders: ' + err.message, 'error');
    } finally {
      setLoading(false);
    }
  };

  const submitOrder = async (): Promise<void> => {
    try {
      const orderData = {
        clientId: orderForm.clientId,
        clientName: orderForm.clientName,
        deliveryAddress: orderForm.deliveryAddress,
        requestedDeliveryDate: orderForm.deliveryDate,
        items: [{
          sku: orderForm.productSku,
          quantity: orderForm.quantity,
          unitPrice: orderForm.unitPrice
        }]
      };

      const response = await axios.post<{ orderId: number }>(`${API_BASE}/orders/submit`, orderData);
      showSnackbar(`Order submitted successfully! Order ID: ${response.data.orderId}`);
      setStatusSearch(response.data.orderId.toString());
      loadStats();
      loadOrders();
    } catch (error) {
      const err = error as AxiosError<{ message: string }>;
      showSnackbar('Error submitting order: ' + (err.response?.data?.message || err.message), 'error');
    }
  };

  const getOrderStatus = async (): Promise<void> => {
    if (!statusSearch) {
      showSnackbar('Please enter an Order ID', 'error');
      return;
    }

    try {
      const response = await axios.get<Order>(`${API_BASE}/orders/${statusSearch}/status`);
      setOrderDetails(response.data);
      setDetailsDialog(true);
      if (response.data.shipmentInfo && response.data.shipmentInfo.shipmentId) {
        setShipmentId(response.data.shipmentInfo.shipmentId.toString());
      }
    } catch (error) {
      showSnackbar('Order not found', 'error');
    }
  };

  const warehouseOperation = async (operation: string): Promise<void> => {
    if (!shipmentId) {
      showSnackbar('Please enter a Shipment ID', 'error');
      return;
    }

    try {
      await axios.post(`${API_BASE}/warehouse/shipments/${shipmentId}/${operation}`);
      showSnackbar(`${operation.replace('-', ' ')} completed successfully`);
      loadStats();
      loadOrders();
    } catch (error) {
      const err = error as AxiosError<string>;
      showSnackbar(`Error: ${err.response?.data || err.message}`, 'error');
    }
  };

  const getStatusColor = (status: string): 'info' | 'success' | 'warning' | 'secondary' | 'error' | 'default' => {
    const colors: Record<string, 'info' | 'success' | 'warning' | 'secondary' | 'error' | 'default'> = {
      RECEIVED: 'info',
      VALIDATED: 'success',
      FULFILLED: 'warning',
      IN_TRANSIT: 'secondary',
      DELIVERED: 'success',
      CANCELLED: 'error'
    };
    return colors[status] || 'default';
  };

  // Handlers
  const handleClientIdChange = (event: ChangeEvent<HTMLInputElement>): void => {
    setOrderForm(prev => ({ ...prev, clientId: event.target.value }));
  };
  const handleClientNameChange = (event: ChangeEvent<HTMLInputElement>): void => {
    setOrderForm(prev => ({ ...prev, clientName: event.target.value }));
  };
  const handleDeliveryAddressChange = (event: ChangeEvent<HTMLTextAreaElement>): void => {
    setOrderForm(prev => ({ ...prev, deliveryAddress: event.target.value }));
  };
  const handleDeliveryDateChange = (event: ChangeEvent<HTMLInputElement>): void => {
    setOrderForm(prev => ({ ...prev, deliveryDate: event.target.value }));
  };
  const handleProductSkuChange = (event: ChangeEvent<{ value: unknown }>): void => {
    setOrderForm(prev => ({ ...prev, productSku: event.target.value as string }));
  };
  const handleQuantityChange = (event: ChangeEvent<HTMLInputElement>): void => {
    setOrderForm(prev => ({ ...prev, quantity: Number(event.target.value) }));
  };
  const handleUnitPriceChange = (event: ChangeEvent<HTMLInputElement>): void => {
    setOrderForm(prev => ({ ...prev, unitPrice: Number(event.target.value) }));
  };

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Box sx={{ flexGrow: 1, backgroundColor: '#0c0c0c', minHeight: '100vh' }}>
        <AppBar position="static" sx={{
          backgroundColor: '#1a1a1a',
          borderBottom: '1px solid #333',
          boxShadow: '0 2px 10px rgba(0,0,0,0.3)'
        }}>
          <Toolbar>
            <Dashboard sx={{ mr: 2 }} />
            <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
              Logistics AI Agent Dashboard
            </Typography>
            <FormControlLabel
              control={
                <Switch
                  checked={aiActive}
                  onChange={(e: React.ChangeEvent<HTMLInputElement>) => setAiActive(e.target.checked)}
                />
              }
              label="AI Agents"
              sx={{ color: 'white', mr: 2 }}
            />
            <FormControlLabel
              control={
                <Switch
                  checked={autoRefresh}
                  onChange={(e: React.ChangeEvent<HTMLInputElement>) => setAutoRefresh(e.target.checked)}
                />
              }
              label="Auto Refresh"
              sx={{ color: 'white' }}
            />
          </Toolbar>
        </AppBar>

        <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
          {/* Statistics Cards */}
          <Grid container spacing={3} sx={{ mb: 3 }}>
            <Grid item xs={12} sm={6} md={3}>
              <Card sx={{
                backgroundColor: '#1a1a1a',
                border: '1px solid #333',
                borderRadius: '12px',
                '&:hover': { backgroundColor: '#222' }
              }}>
                <CardContent>
                  <Typography variant="h4" sx={{ color: '#10a37f', fontWeight: 'bold' }}>{stats.totalOrders || 0}</Typography>
                  <Typography variant="body2" sx={{ color: '#aaa' }}>Total Orders</Typography>
                </CardContent>
              </Card>
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <Card sx={{
                backgroundColor: '#1a1a1a',
                border: '1px solid #333',
                borderRadius: '12px',
                '&:hover': { backgroundColor: '#222' }
              }}>
                <CardContent>
                  <Typography variant="h4" sx={{ color: '#ff6b35', fontWeight: 'bold' }}>{stats.receivedOrders || 0}</Typography>
                  <Typography variant="body2" sx={{ color: '#aaa' }}>Received</Typography>
                </CardContent>
              </Card>
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <Card sx={{
                backgroundColor: '#1a1a1a',
                border: '1px solid #333',
                borderRadius: '12px',
                '&:hover': { backgroundColor: '#222' }
              }}>
                <CardContent>
                  <Typography variant="h4" sx={{ color: '#f59e0b', fontWeight: 'bold' }}>{stats.inTransitOrders || 0}</Typography>
                  <Typography variant="body2" sx={{ color: '#aaa' }}>In Transit</Typography>
                </CardContent>
              </Card>
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <Card sx={{
                backgroundColor: '#1a1a1a',
                border: '1px solid #333',
                borderRadius: '12px',
                '&:hover': { backgroundColor: '#222' }
              }}>
                <CardContent>
                  <Typography variant="h4" sx={{ color: '#10a37f', fontWeight: 'bold' }}>{stats.deliveredOrders || 0}</Typography>
                  <Typography variant="body2">Delivered</Typography>
                </CardContent>
              </Card>
            </Grid>
          </Grid>

          {/* Submit New Order */}
          <Grid container spacing={3} sx={{ mb: 3 }}>
            <Grid item xs={12} md={6}>
              <Card>
                <CardContent>
                  <Typography variant="h6" gutterBottom>Submit New Order</Typography>
                  <Grid container spacing={2}>
                    <Grid item xs={12}>
                      <TextField
                        fullWidth
                        label="Client ID"
                        value={orderForm.clientId}
                        onChange={handleClientIdChange}
                      />
                    </Grid>
                    <Grid item xs={12}>
                      <TextField
                        fullWidth
                        label="Client Name"
                        value={orderForm.clientName}
                        onChange={handleClientNameChange}
                      />
                    </Grid>
                    <Grid item xs={12}>
                      <TextField
                        fullWidth
                        multiline
                        rows={2}
                        label="Delivery Address"
                        value={orderForm.deliveryAddress}
                        onChange={handleDeliveryAddressChange}
                      />
                    </Grid>
                    <Grid item xs={12}>
                      <TextField
                        fullWidth
                        type="datetime-local"
                        label="Delivery Date"
                        InputLabelProps={{ shrink: true }}
                        value={orderForm.deliveryDate}
                        onChange={handleDeliveryDateChange}
                      />
                    </Grid>
                    <Grid item xs={12}>
                      <FormControl fullWidth>
                        <InputLabel>Product SKU</InputLabel>
                        <Select
                          value={orderForm.productSku}
                          onChange={handleProductSkuChange}
                          label="Product SKU"
                        >
                          <MenuItem value="TILE-001">TILE-001 - Ceramic Floor Tiles</MenuItem>
                          <MenuItem value="TILE-002">TILE-002 - Marble Wall Tiles</MenuItem>
                          <MenuItem value="CONC-001">CONC-001 - Portland Cement</MenuItem>
                          <MenuItem value="ROOF-001">ROOF-001 - Clay Roof Tiles</MenuItem>
                          <MenuItem value="PLUMB-001">PLUMB-001 - PVC Pipes</MenuItem>
                        </Select>
                      </FormControl>
                    </Grid>
                    <Grid item xs={6}>
                      <TextField
                        fullWidth
                        type="number"
                        label="Quantity"
                        value={orderForm.quantity}
                        onChange={handleQuantityChange}
                      />
                    </Grid>
                    <Grid item xs={6}>
                      <TextField
                        fullWidth
                        type="number"
                        label="Unit Price (€)"
                        inputProps={{ step: "0.01" }}
                        value={orderForm.unitPrice}
                        onChange={handleUnitPriceChange}
                      />
                    </Grid>
                    <Grid item xs={12}>
                      <Button
                        fullWidth
                        variant="contained"
                        startIcon={<Send />}
                        onClick={submitOrder}
                        sx={{ mt: 2 }}
                      >
                        Submit Order
                      </Button>
                    </Grid>
                  </Grid>
                </CardContent>
              </Card>
            </Grid>
          </Grid>

          {/* Order Status Check and Warehouse Operations */}
          <Grid container spacing={3} sx={{ mb: 3 }}>
            <Grid item xs={12} md={6}>
              <Card>
                <CardContent>
                  <Typography variant="h6" gutterBottom>Order Status Check</Typography>
                  <Box sx={{ display: 'flex', gap: 1, mb: 2 }}>
                    <TextField
                      label="Order ID"
                      value={statusSearch}
                      onChange={(e: React.ChangeEvent<HTMLInputElement>) => setStatusSearch(e.target.value)}
                      sx={{ flexGrow: 1 }}
                    />
                    <Button variant="contained" startIcon={<Search />} onClick={getOrderStatus}>
                      Search
                    </Button>
                  </Box>
                </CardContent>
              </Card>
            </Grid>
            <Grid item xs={12} md={6}>
              <Card>
                <CardContent>
                  <Typography variant="h6" gutterBottom>Warehouse Operations</Typography>
                  <TextField
                    fullWidth
                    label="Shipment ID"
                    value={shipmentId}
                    onChange={(e: React.ChangeEvent<HTMLInputElement>) => setShipmentId(e.target.value)}
                    sx={{ mb: 2 }}
                  />
                  <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                    <Button size="small" variant="outlined" onClick={() => warehouseOperation('start-loading')}>
                      Start Loading
                    </Button>
                    <Button size="small" variant="outlined" onClick={() => warehouseOperation('complete-loading')}>
                      Complete
                    </Button>
                    <Button size="small" variant="outlined" onClick={() => warehouseOperation('dispatch')}>
                      Dispatch
                    </Button>
                    <Button size="small" variant="outlined" onClick={() => warehouseOperation('delivered')}>
                      Delivered
                    </Button>
                  </Box>
                </CardContent>
              </Card>
            </Grid>
          </Grid>

          {/* Orders Table */}
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                <Typography variant="h6" sx={{ flexGrow: 1 }}>Recent Orders</Typography>
                {loading && <LinearProgress sx={{ width: '200px', mr: 2 }} />}
                <Button variant="outlined" startIcon={<Refresh />} onClick={() => { loadStats(); loadOrders(); }}>
                  Refresh
                </Button>
              </Box>
              <TableContainer component={Paper} sx={{ maxHeight: 400 }}>
                <Table stickyHeader size="small" aria-label="orders table">
                  <TableHead>
                    <TableRow>
                      <TableCell>Order ID</TableCell>
                      <TableCell>Client Name</TableCell>
                      <TableCell>Status</TableCell>
                      <TableCell>Order Date</TableCell>
                      <TableCell>Estimated Delivery</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {orders.map((order) => (
                      <TableRow
                        key={order.orderId}
                        hover
                        sx={{ cursor: 'pointer' }}
                        onClick={() => {
                          setOrderDetails(order);
                          setDetailsDialog(true);
                          if (order.shipmentInfo?.shipmentId) setShipmentId(order.shipmentInfo.shipmentId.toString());
                        }}
                      >
                        <TableCell>{order.orderId}</TableCell>
                        <TableCell>{order.clientName}</TableCell>
                        <TableCell>
                          <Chip
                            label={order.status}
                            color={getStatusColor(order.status)}
                            size="small"
                          />
                        </TableCell>
                        <TableCell>{new Date(order.orderDate).toLocaleString()}</TableCell>
                        <TableCell>{order.estimatedDelivery ? new Date(order.estimatedDelivery).toLocaleString() : '-'}</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </CardContent>
          </Card>

          {/* Order Details Dialog */}
          <Dialog
            open={detailsDialog}
            onClose={() => setDetailsDialog(false)}
            maxWidth="sm"
            fullWidth
          >
            <DialogTitle>Order Details</DialogTitle>
            <DialogContent dividers>
              {orderDetails ? (
                <Box>
                  <Typography variant="h6" component="div" gutterBottom>
                    Order #{orderDetails.orderId}
                  </Typography>
                  <Typography variant="body1" component="div" sx={{ mb: 1 }}>
                    Client Name: {orderDetails.clientName}
                  </Typography>
                  <Typography variant="body1" component="div" sx={{ mb: 1 }}>
                    Status: <Chip label={orderDetails.status} color={getStatusColor(orderDetails.status)} size="small" sx={{ ml: 1 }} />
                  </Typography>
                  <Typography variant="body1" component="div" sx={{ mb: 1 }}>
                    Order Date: {new Date(orderDetails.orderDate).toLocaleString()}
                  </Typography>
                  <Typography variant="body1" component="div" sx={{ mb: 1 }}>
                    Estimated Delivery: {orderDetails.estimatedDelivery ? new Date(orderDetails.estimatedDelivery).toLocaleString() : 'N/A'}
                  </Typography>
                  {orderDetails.totalItems !== undefined && (
                    <Typography variant="body1" component="div" sx={{ mb: 1 }}>
                      Total Items: {orderDetails.totalItems}
                    </Typography>
                  )}
                  {orderDetails.totalWeight !== undefined && (
                    <Typography variant="body1" component="div" sx={{ mb: 1 }}>
                      Total Weight: {orderDetails.totalWeight} kg
                    </Typography>
                  )}
                </Box>
              ) : (
                <Typography>No order details available</Typography>
              )}
            </DialogContent>
            <DialogActions>
              <Button onClick={() => setDetailsDialog(false)}>Close</Button>
            </DialogActions>
          </Dialog>

          <Snackbar
            open={snackbar.open}
            autoHideDuration={4000}
            onClose={() => setSnackbar({ ...snackbar, open: false })}
            anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
          >
            <Alert
              onClose={() => setSnackbar({ ...snackbar, open: false })}
              severity={snackbar.severity}
              sx={{ width: '100%' }}
            >
              {snackbar.message}
            </Alert>
          </Snackbar>
        </Container>
      </Box>
    </ThemeProvider>
  );
}

export default App;
