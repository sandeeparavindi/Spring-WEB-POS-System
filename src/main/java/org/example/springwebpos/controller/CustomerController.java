package org.example.springwebpos.controller;

import lombok.RequiredArgsConstructor;
import org.example.springwebpos.customObj.CustomerResponse;
import org.example.springwebpos.dto.CustomerDTO;
import org.example.springwebpos.exception.CustomerNotFoundException;
import org.example.springwebpos.exception.DataPersistFailedException;
import org.example.springwebpos.service.CustomerService;
import org.example.springwebpos.util.AppUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    @Autowired
    private final CustomerService customerService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> saveCustomer(
            @RequestPart("name") String name,
            @RequestPart("address") String address,
            @RequestPart("mobile") String mobile,
            @RequestPart("profilePic") MultipartFile profilePic) {
        logger.info("Received request to save customer with name: {}", name);
        try {
            byte[] imageBytes = profilePic.getBytes();
            String base64ProfilePic = AppUtil.toBase64ProfilePic(imageBytes);

            CustomerDTO buildCustomerDTO = new CustomerDTO();
            buildCustomerDTO.setName(name);
            buildCustomerDTO.setAddress(address);
            buildCustomerDTO.setMobile(mobile);
            buildCustomerDTO.setProfilePic(base64ProfilePic);

            customerService.saveCustomer(buildCustomerDTO);
            logger.info("Customer saved successfully with name: {}", name);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (DataPersistFailedException e) {
            logger.error("Failed to save customer: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Error occurred while saving customer: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateCustomer(
            @PathVariable("id") String id,
            @RequestPart("updateName") String updateName,
            @RequestPart("updateAddress") String updateAddress,
            @RequestPart("updateMobile") String updateMobile,
            @RequestPart("updateProfilePic") MultipartFile updateProfilePic
    ) {
        logger.info("Received request to update customer with ID: {}", id);
        try {
            byte[] imageBytes = updateProfilePic.getBytes();
            String updateBase64ProfilePic = AppUtil.toBase64ProfilePic(imageBytes);

            var updateCustomer = new CustomerDTO();
            updateCustomer.setId(id);
            updateCustomer.setName(updateName);
            updateCustomer.setAddress(updateAddress);
            updateCustomer.setMobile(updateMobile);
            updateCustomer.setProfilePic(updateBase64ProfilePic);

            customerService.updateCustomer(updateCustomer);
            logger.info("Customer updated successfully with ID: {}", id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (CustomerNotFoundException e) {
            logger.warn("Customer not found for update with ID: {}", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Error occurred while updating customer with ID: {}", id, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable("id") String customerId) {
        logger.info("Received request to delete customer with ID: {}", customerId);
        try {
            customerService.deleteCustomer(customerId);
            logger.info("Customer deleted successfully with ID: {}", customerId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (CustomerNotFoundException e) {
            logger.warn("Customer not found for delete with ID: {}", customerId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Error occurred while deleting customer with ID: {}", customerId, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CustomerResponse getSelectedCustomer(@PathVariable ("id") String id)  {
        logger.info("Received request to get selected customer with ID: {}", id);
        return customerService.getSelectedCustomer(id);
    }

    @GetMapping(value = "allcustomers", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CustomerDTO> getAllCustomers(){
        logger.info("Received request to get all customers");
        return customerService.getAllCustomers();
    }

}
