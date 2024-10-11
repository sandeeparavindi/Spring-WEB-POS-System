package org.example.springwebpos.service;

import lombok.RequiredArgsConstructor;
import org.example.springwebpos.controller.OrderController;
import org.example.springwebpos.dao.CustomerDAO;
import org.example.springwebpos.dao.ItemDAO;
import org.example.springwebpos.dao.OrderDAO;
import org.example.springwebpos.dto.OrderDTO;
import org.example.springwebpos.entity.CustomerEntity;
import org.example.springwebpos.entity.ItemEntity;
import org.example.springwebpos.entity.OrderDetailEntity;
import org.example.springwebpos.entity.OrderEntity;
import org.example.springwebpos.util.AppUtil;
import org.example.springwebpos.util.Mapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class OderServiceIMPL implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    @Autowired
    private final OrderDAO orderDAO;
    @Autowired
    private final CustomerDAO customerDAO;
    @Autowired
    private final ItemDAO itemDAO;
    @Autowired
    private final Mapping mapping;
    @Override
    public OrderDTO saveOrder(OrderDTO orderDTO) {
        logger.info("Saving order for customer ID: {}", orderDTO.getCustomerId());
        if (orderDTO.getOrderId() == null || orderDTO.getOrderId().isEmpty()) {
            orderDTO.setOrderId(AppUtil.createOrderId());
            logger.debug("Generated new order ID: {}", orderDTO.getOrderId());
        }

        Optional<CustomerEntity> customerOpt = customerDAO.findById(orderDTO.getCustomerId());
        if (!customerOpt.isPresent()) {
            logger.error("Customer ID {} not found", orderDTO.getCustomerId());
            throw new RuntimeException("Customer not found with ID: " + orderDTO.getCustomerId());
        }
        CustomerEntity customer = customerOpt.get();
        logger.debug("Customer name: {}", customer.getName());

        OrderEntity orderEntity = mapping.convertToOrderEntity(orderDTO);
        orderEntity.setCustomer(customer);

        logger.debug("Mapping order details");
        orderEntity.setOrderDetails(orderDTO.getOrderDetails().stream().map(orderDetailDTO -> {
            OrderDetailEntity orderDetail = new OrderDetailEntity();
            orderDetail.setOrder(orderEntity);

            Optional<ItemEntity> itemOpt = itemDAO.findById(orderDetailDTO.getItemCode());
            if (!itemOpt.isPresent()) {
                logger.error("Item ID {} not found", orderDetailDTO.getItemCode());
                throw new RuntimeException("Item not found with code: " + orderDetailDTO.getItemCode());
            }
            ItemEntity item = itemOpt.get();
            logger.debug("Found item: {}", item.getDescription());

            //check if  sufficient qty is available
            if (item.getQty() < orderDetailDTO.getQuantity()) {
                logger.error("Insufficient quantity for item: {} (requested: {}, available: {})",
                        item.getCode(), orderDetailDTO.getQuantity(), item.getQty());
                throw new RuntimeException("Insufficient quantity for item: " + item.getCode());
            }

            //update item qty
            item.setQty(item.getQty() - orderDetailDTO.getQuantity());
            logger.info("Updated item quantity for item: {}. New quantity: {}", item.getCode(), item.getQty());
            itemDAO.save(item);

            //set order detail
            orderDetail.setItem(item);
            orderDetail.setQuantity(orderDetailDTO.getQuantity());
            orderDetail.setUnitPrice(orderDetailDTO.getUnitPrice());
            orderDetail.setTotalPrice(orderDetailDTO.getTotalPrice());

            return orderDetail;
        }).collect(Collectors.toList()));

        double subTotal = orderEntity.getOrderDetails().stream()
                .mapToDouble(OrderDetailEntity::getTotalPrice)
                .sum();
        orderEntity.setSubTotal(subTotal);
        logger.debug("Calculated subtotal: {}", subTotal);

        orderEntity.setTotal(subTotal - orderEntity.getDiscount());
        logger.debug("Total after discount: {}", orderEntity.getTotal());

        OrderEntity savedOrder = orderDAO.save(orderEntity);
        logger.info("Saved order: {}", savedOrder.getOrderId());

        return mapping.convertToOrderDTO(savedOrder);
    }
}
