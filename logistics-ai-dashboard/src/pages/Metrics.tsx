import React, { useState, useEffect } from 'react';
import { 
  Box, 
  Grid, 
  Card, 
  CardContent, 
  Typography, 
  Alert,
  CircularProgress,
  Button,
  Chip,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper
} from '@mui/material';
import { 
  LineChart, 
  Line, 
  XAxis, 
  YAxis, 
  CartesianGrid, 
  Tooltip, 
  ResponsiveContainer,
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell
} from 'recharts';
import RefreshIcon from '@mui/icons-material/Refresh';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import MemoryIcon from '@mui/icons-material/Memory';
import SpeedIcon from '@mui/icons-material/Speed';
import api from '../services/api';

interface MetricsData {
  totalOrdersProcessed: number;
  totalErrors: number;
  errorRate: string;
  averageProcessingTime: string;
  throughput: string;
  elapsedTime: string;
  ordersByStatus: Record<string, number>;
  systemMetrics: {
    usedMemory: string;
    totalMemory: string;
    memoryUsage: string;
    availableProcessors: number;
  };
  status: string;
  healthy: boolean;
}

interface AIMetrics {
  totalOrdersProcessed: number;
  automationSuccessRate: number;
  averageProcessingTime: string;
  costSavings: string;
  uptime: string;
}

const Metrics: React.FC = () => {
  const [metrics, setMetrics] = useState<MetricsData | null>(null);
  const [aiMetrics, setAiMetrics] = useState<AIMetrics | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [lastUpdate, setLastUpdate] = useState<Date>(new Date());

  const fetchMetrics = async () => {
    try {
      setLoading(true);
      setError(null);

      const response = await api.get('/api/metrics/current');
      setMetrics(response.data);

      if (response.data.aiMetrics) {
        setAiMetrics(response.data.aiMetrics);
      }

      setLastUpdate(new Date());
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load metrics');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMetrics();
    const interval = setInterval(fetchMetrics, 30000); // Refresh every 30 seconds
    return () => clearInterval(interval);
  }, []);

  const statusData = metrics?.ordersByStatus ? 
    Object.entries(metrics.ordersByStatus).map(([status, count]) => ({
      name: status.replace(/_/g, ' '),
      value: count
    })) : [];

  const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884d8'];

  if (loading && !metrics) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4" component="h1" gutterBottom>
          System Metrics & Performance
        </Typography>
        <Box>
          <Typography variant="body2" color="textSecondary" sx={{ mr: 2, display: 'inline' }}>
            Last updated: {lastUpdate.toLocaleTimeString()}
          </Typography>
          <Button
            variant="outlined"
            startIcon={<RefreshIcon />}
            onClick={fetchMetrics}
            disabled={loading}
            size="small"
          >
            Refresh
          </Button>
        </Box>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      {metrics && (
        <>
          {/* Status Overview */}
          <Grid container spacing={3} sx={{ mb: 3 }}>
            <Grid item xs={12} md={3}>
              <Card>
                <CardContent>
                  <Box display="flex" alignItems="center">
                    <TrendingUpIcon color="primary" sx={{ mr: 2 }} />
                    <Box>
                      <Typography variant="h6" component="div">
                        {metrics.totalOrdersProcessed}
                      </Typography>
                      <Typography variant="body2" color="textSecondary">
                        Orders Processed
                      </Typography>
                    </Box>
                  </Box>
                </CardContent>
              </Card>
            </Grid>

            <Grid item xs={12} md={3}>
              <Card>
                <CardContent>
                  <Box display="flex" alignItems="center">
                    <SpeedIcon color="secondary" sx={{ mr: 2 }} />
                    <Box>
                      <Typography variant="h6" component="div">
                        {metrics.throughput}
                      </Typography>
                      <Typography variant="body2" color="textSecondary">
                        Throughput
                      </Typography>
                    </Box>
                  </Box>
                </CardContent>
              </Card>
            </Grid>

            <Grid item xs={12} md={3}>
              <Card>
                <CardContent>
                  <Box display="flex" alignItems="center">
                    <MemoryIcon color="info" sx={{ mr: 2 }} />
                    <Box>
                      <Typography variant="h6" component="div">
                        {metrics.systemMetrics.memoryUsage}
                      </Typography>
                      <Typography variant="body2" color="textSecondary">
                        Memory Usage
                      </Typography>
                    </Box>
                  </Box>
                </CardContent>
              </Card>
            </Grid>

            <Grid item xs={12} md={3}>
              <Card>
                <CardContent>
                  <Box display="flex" alignItems="center">
                    <Chip 
                      label={metrics.status} 
                      color={metrics.status === 'ACTIVE' ? 'success' : 'default'}
                      variant="outlined"
                    />
                    <Box ml={2}>
                      <Typography variant="body2" color="textSecondary">
                        System Status
                      </Typography>
                    </Box>
                  </Box>
                </CardContent>
              </Card>
            </Grid>
          </Grid>

          {/* Performance Metrics */}
          <Grid container spacing={3} sx={{ mb: 3 }}>
            <Grid item xs={12} md={6}>
              <Card>
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    Processing Performance
                  </Typography>
                  <Table size="small">
                    <TableBody>
                      <TableRow>
                        <TableCell>Average Processing Time</TableCell>
                        <TableCell align="right">{metrics.averageProcessingTime}</TableCell>
                      </TableRow>
                      <TableRow>
                        <TableCell>Error Rate</TableCell>
                        <TableCell align="right">
                          <Chip 
                            label={metrics.errorRate} 
                            size="small"
                            color={parseFloat(metrics.errorRate) < 5 ? 'success' : 'warning'}
                          />
                        </TableCell>
                      </TableRow>
                      <TableRow>
                        <TableCell>Total Errors</TableCell>
                        <TableCell align="right">{metrics.totalErrors}</TableCell>
                      </TableRow>
                      <TableRow>
                        <TableCell>Elapsed Time</TableCell>
                        <TableCell align="right">{metrics.elapsedTime}</TableCell>
                      </TableRow>
                    </TableBody>
                  </Table>
                </CardContent>
              </Card>
            </Grid>

            <Grid item xs={12} md={6}>
              <Card>
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    System Resources
                  </Typography>
                  <Table size="small">
                    <TableBody>
                      <TableRow>
                        <TableCell>Used Memory</TableCell>
                        <TableCell align="right">{metrics.systemMetrics.usedMemory}</TableCell>
                      </TableRow>
                      <TableRow>
                        <TableCell>Total Memory</TableCell>
                        <TableCell align="right">{metrics.systemMetrics.totalMemory}</TableCell>
                      </TableRow>
                      <TableRow>
                        <TableCell>Memory Usage</TableCell>
                        <TableCell align="right">
                          <Chip 
                            label={metrics.systemMetrics.memoryUsage} 
                            size="small"
                            color={parseFloat(metrics.systemMetrics.memoryUsage) < 70 ? 'success' : 'warning'}
                          />
                        </TableCell>
                      </TableRow>
                      <TableRow>
                        <TableCell>CPU Cores</TableCell>
                        <TableCell align="right">{metrics.systemMetrics.availableProcessors}</TableCell>
                      </TableRow>
                    </TableBody>
                  </Table>
                </CardContent>
              </Card>
            </Grid>
          </Grid>

          {/* Order Status Distribution */}
          {statusData.length > 0 && (
            <Grid container spacing={3} sx={{ mb: 3 }}>
              <Grid item xs={12} md={8}>
                <Card>
                  <CardContent>
                    <Typography variant="h6" gutterBottom>
                      Order Status Distribution
                    </Typography>
                    <ResponsiveContainer width="100%" height={300}>
                      <BarChart data={statusData}>
                        <CartesianGrid strokeDasharray="3 3" />
                        <XAxis dataKey="name" />
                        <YAxis />
                        <Tooltip />
                        <Bar dataKey="value" fill="#8884d8" />
                      </BarChart>
                    </ResponsiveContainer>
                  </CardContent>
                </Card>
              </Grid>

              <Grid item xs={12} md={4}>
                <Card>
                  <CardContent>
                    <Typography variant="h6" gutterBottom>
                      Status Breakdown
                    </Typography>
                    <ResponsiveContainer width="100%" height={300}>
                      <PieChart>
                        <Pie
                          data={statusData}
                          cx="50%"
                          cy="50%"
                          labelLine={false}
                          label={({name, percent}) => `${name}: ${(percent * 100).toFixed(0)}%`}
                          outerRadius={80}
                          fill="#8884d8"
                          dataKey="value"
                        >
                          {statusData.map((entry, index) => (
                            <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                          ))}
                        </Pie>
                        <Tooltip />
                      </PieChart>
                    </ResponsiveContainer>
                  </CardContent>
                </Card>
              </Grid>
            </Grid>
          )}

          {/* AI Metrics */}
          {aiMetrics && (
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  AI Agent Performance
                </Typography>
                <Grid container spacing={2}>
                  <Grid item xs={12} md={2.4}>
                    <Box textAlign="center" p={2}>
                      <Typography variant="h4" color="primary">
                        {aiMetrics.totalOrdersProcessed}
                      </Typography>
                      <Typography variant="body2" color="textSecondary">
                        Total Processed
                      </Typography>
                    </Box>
                  </Grid>
                  <Grid item xs={12} md={2.4}>
                    <Box textAlign="center" p={2}>
                      <Typography variant="h4" color="success.main">
                        {aiMetrics.automationSuccessRate}%
                      </Typography>
                      <Typography variant="body2" color="textSecondary">
                        Success Rate
                      </Typography>
                    </Box>
                  </Grid>
                  <Grid item xs={12} md={2.4}>
                    <Box textAlign="center" p={2}>
                      <Typography variant="h4" color="info.main">
                        {aiMetrics.averageProcessingTime}
                      </Typography>
                      <Typography variant="body2" color="textSecondary">
                        Avg Processing Time
                      </Typography>
                    </Box>
                  </Grid>
                  <Grid item xs={12} md={2.4}>
                    <Box textAlign="center" p={2}>
                      <Typography variant="h4" color="secondary.main">
                        {aiMetrics.costSavings}
                      </Typography>
                      <Typography variant="body2" color="textSecondary">
                        Cost Savings
                      </Typography>
                    </Box>
                  </Grid>
                  <Grid item xs={12} md={2.4}>
                    <Box textAlign="center" p={2}>
                      <Typography variant="h4" color="primary">
                        {aiMetrics.uptime}
                      </Typography>
                      <Typography variant="body2" color="textSecondary">
                        System Uptime
                      </Typography>
                    </Box>
                  </Grid>
                </Grid>
              </CardContent>
            </Card>
          )}
        </>
      )}
    </Box>
  );
};

export default Metrics;
