package org.example.springwebpos.service;

import lombok.RequiredArgsConstructor;
import org.example.springwebpos.customObj.CustomerErrorResponse;
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
        Optional<CustomerEntity> tmpCustomer = customerDAO.findById(customerDTO.getId());
        if (!tmpCustomer.isPresent()) {
            throw new CustomerNotFoundException("Customer Not Found");
        } else {
            tmpCustomer.get().setName(customerDTO.getName());
            tmpCustomer.get().setAddress(customerDTO.getAddress());
            tmpCustomer.get().setMobile(customerDTO.getMobile());
            tmpCustomer.get().setProfilePic(customerDTO.getProfilePic());
            customerDAO.save(tmpCustomer.get());
        }
    }

    @Override
    public void deleteCustomer(String customerId) {
        Optional<CustomerEntity> selectedCustomerId = customerDAO.findById(customerId);
        if (!selectedCustomerId.isPresent()) {
            throw new CustomerNotFoundException("Customer not found");
        } else {
            customerDAO.deleteById(customerId);
        }
    }

    @Override
    public CustomerResponse getSelectedCustomer(String customerId) {
        if(customerDAO.existsById(customerId)){
            return mapping.convertToCUstomerDTO(customerDAO.getReferenceById(customerId));
        }else {
            return new CustomerErrorResponse(0,"Customer not found");
        }
    }

    @Override
    public List<CustomerDTO> getAllCustomers() {
        return List.of();
    }
}
