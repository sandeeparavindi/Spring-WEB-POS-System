package org.example.springwebpos.service;

import org.example.springwebpos.dto.OrderDTO;

public interface OrderService {
    OrderDTO saveOrder(OrderDTO orderDTO);
}
