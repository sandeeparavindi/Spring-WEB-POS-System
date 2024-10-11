package org.example.springwebpos.service;

import lombok.RequiredArgsConstructor;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class OderServiceIMPL implements OrderService {
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
        if (orderDTO.getOrderId() == null || orderDTO.getOrderId().isEmpty()) {
            orderDTO.setOrderId(AppUtil.createOrderId());
        }

        Optional<CustomerEntity> customerOpt = customerDAO.findById(orderDTO.getCustomerId());
        if (!customerOpt.isPresent()) {
            throw new RuntimeException("Customer not found with ID: " + orderDTO.getCustomerId());
        }
        CustomerEntity customer = customerOpt.get();

        OrderEntity orderEntity = mapping.convertToOrderEntity(orderDTO);
        orderEntity.setCustomer(customer);

        orderEntity.setOrderDetails(orderDTO.getOrderDetails().stream().map(orderDetailDTO -> {
            OrderDetailEntity orderDetail = new OrderDetailEntity();
            orderDetail.setOrder(orderEntity);

            Optional<ItemEntity> itemOpt = itemDAO.findById(orderDetailDTO.getItemCode());
            if (!itemOpt.isPresent()) {
                throw new RuntimeException("Item not found with code: " + orderDetailDTO.getItemCode());
            }
            ItemEntity item = itemOpt.get();

            //check if  sufficient qty is available
            if (item.getQty() < orderDetailDTO.getQuantity()) {
                throw new RuntimeException("Insufficient quantity for item: " + item.getCode());
            }

            //update item qty
            item.setQty(item.getQty() - orderDetailDTO.getQuantity());
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

        orderEntity.setTotal(subTotal - orderEntity.getDiscount());

        OrderEntity savedOrder = orderDAO.save(orderEntity);

        return mapping.convertToOrderDTO(savedOrder);
    }
}
