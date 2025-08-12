import React from 'react';
import { 
  Drawer, 
  List, 
  ListItem, 
  ListItemIcon, 
  ListItemText, 
  Typography, 
  Box,
  Divider 
} from '@mui/material';
import { useNavigate, useLocation } from 'react-router-dom';
import DashboardIcon from '@mui/icons-material/Dashboard';
import ShoppingCartIcon from '@mui/icons-material/ShoppingCart';
import WarehouseIcon from '@mui/icons-material/Warehouse';
import AnalyticsIcon from '@mui/icons-material/Analytics';
import TestingIcon from '@mui/icons-material/BugReport';

const drawerWidth = 240;

const menuItems = [
  { text: 'Dashboard', icon: <DashboardIcon />, path: '/' },
  { text: 'Orders', icon: <ShoppingCartIcon />, path: '/orders' },
  { text: 'Warehouse', icon: <WarehouseIcon />, path: '/warehouse' },
  { text: 'Metrics', icon: <AnalyticsIcon />, path: '/metrics' },
  { text: 'Testing', icon: <TestingIcon />, path: '/testing' },
];

const Sidebar: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();

  return (
    <Drawer
      variant="permanent"
      sx={{
        width: drawerWidth,
        flexShrink: 0,
        '& .MuiDrawer-paper': {
          width: drawerWidth,
          boxSizing: 'border-box',
          backgroundColor: '#1565c0',
          color: 'white',
        },
      }}
    >
      <Box sx={{ p: 2, textAlign: 'center' }}>
        <Typography variant="h6" component="div" sx={{ fontWeight: 'bold', color: 'white' }}>
          Logistics AI Agent
        </Typography>
        <Typography variant="body2" sx={{ color: '#bbdefb', mt: 0.5 }}>
          Production Dashboard
        </Typography>
      </Box>
      <Divider sx={{ backgroundColor: '#1e88e5' }} />

      <List>
        {menuItems.map((item) => (
          <ListItem
            key={item.text}
            button
            onClick={() => navigate(item.path)}
            sx={{
              backgroundColor: location.pathname === item.path ? '#1e88e5' : 'transparent',
              '&:hover': {
                backgroundColor: '#1e88e5',
              },
              borderRadius: '0 25px 25px 0',
              mr: 2,
              mb: 0.5,
            }}
          >
            <ListItemIcon sx={{ color: 'white', minWidth: 40 }}>
              {item.icon}
            </ListItemIcon>
            <ListItemText 
              primary={item.text} 
              sx={{ 
                color: 'white',
                '& .MuiListItemText-primary': {
                  fontSize: '0.95rem',
                  fontWeight: location.pathname === item.path ? 600 : 400,
                },
              }}
            />
          </ListItem>
        ))}
      </List>
    </Drawer>
  );
};

export default Sidebar;
