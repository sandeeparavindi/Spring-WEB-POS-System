package org.example.springwebpos.service;

import lombok.RequiredArgsConstructor;
import org.example.springwebpos.customObj.CustomerResponse;
import org.example.springwebpos.dao.CustomerDAO;
import org.example.springwebpos.dto.CustomerDTO;
import org.example.springwebpos.entity.CustomerEntity;
import org.example.springwebpos.exception.CustomerNotFoundException;
import org.example.springwebpos.exception.DataPersistFailedException;
import org.example.springwebpos.util.AppUtil;
import org.example.springwebpos.util.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class CustomerServiceIMPL implements CustomerService {
    @Autowired
    private final CustomerDAO customerDAO;

    @Autowired
    private final Mapping mapping;

    @Override
    public void saveCustomer(CustomerDTO customerDTO) {
        customerDTO.setId(AppUtil.createCustomerId());
        CustomerEntity savedCustomer = customerDAO.save(mapping.convertToCustomerEntity(customerDTO));
        if (savedCustomer == null) {
            throw new DataPersistFailedException("Cannot save data");
        }
    }

    @Override
    public void updateCustomer(CustomerDTO customerDTO) {

    }

    @Override
    public void deleteCustomer(String customerId) {

    }

    @Override
    public CustomerResponse getSelectedCustomer(String customerId) {
        return null;
    }

    @Override
    public List<CustomerDTO> getAllCustomers() {
        return List.of();
    }
}
