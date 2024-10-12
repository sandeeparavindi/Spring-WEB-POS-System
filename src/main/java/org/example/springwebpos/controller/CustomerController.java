package org.example.springwebpos.controller;

import lombok.RequiredArgsConstructor;
import org.example.springwebpos.customObj.CustomerErrorResponse;
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

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    @Autowired
    private final CustomerService customerService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CustomerErrorResponse> saveCustomer(
            @RequestPart(value = "name", required = false) String name,
            @RequestPart(value = "address", required = false) String address,
            @RequestPart(value = "mobile", required = false) String mobile,
            @RequestPart(value = "profilePic", required = false) MultipartFile profilePic) {
        logger.info("Request to save customer: Name={}, Address={}", name, address);

        if (name == null) {
            logger.error("Name is missing");
            return new ResponseEntity<>(new CustomerErrorResponse("error", "Name is required"),
                    HttpStatus.BAD_REQUEST);
        }
        if (!name.matches("^[a-zA-Z ]{3,20}$")) {
            logger.error("Invalid name: Name must only contain letters and be between 3 and 20 characters long");
            return new ResponseEntity<>(new CustomerErrorResponse("error", "Name must only contain" +
                    " letters and be between 3 and 20 characters long"), HttpStatus.BAD_REQUEST);
        }

        if (mobile == null) {
            logger.error("Mobile number is missing");
            return new ResponseEntity<>(new CustomerErrorResponse("error",
                    "Mobile number is required"), HttpStatus.BAD_REQUEST);
        }
        if (!mobile.matches("^0\\d{9}$")) {
            logger.error("Invalid mobile number: Mobile must be exactly 10 digits");
            return new ResponseEntity<>(new CustomerErrorResponse("error",
                    "Mobile must be exactly 10 digits"), HttpStatus.BAD_REQUEST);
        }

        if (address == null || address.isEmpty()) {
            logger.error("Address is missing");
            return new ResponseEntity<>(new CustomerErrorResponse("error", "Address is required"),
                    HttpStatus.BAD_REQUEST);
        }
        if (!address.matches("^[a-zA-Z0-9, ]{1,100}$")) {
            logger.error("Invalid address: Address can only contain letters, digits, commas, spaces," +
                    " and must be max 100 characters");
            return new ResponseEntity<>(new CustomerErrorResponse("error",
                    "Address can only contain letters, digits, commas, spaces, and must be max 100 characters")
                    , HttpStatus.BAD_REQUEST);
        }

        try {
            if (profilePic == null || profilePic.isEmpty()) {
                logger.error("Profile picture is missing");
                return new ResponseEntity<>(new CustomerErrorResponse("error",
                        "Profile picture is required"), HttpStatus.BAD_REQUEST);
            }
            byte[] imageBytes = profilePic.getBytes();
            String base64ProfilePic = AppUtil.toBase64ProfilePic(imageBytes);

            CustomerDTO buildCustomerDTO = new CustomerDTO();
            buildCustomerDTO.setName(name);
            buildCustomerDTO.setAddress(address);
            buildCustomerDTO.setMobile(mobile);
            buildCustomerDTO.setProfilePic(base64ProfilePic);

            customerService.saveCustomer(buildCustomerDTO);
            logger.info("Customer saved successfully: Name={}", name);
            return new ResponseEntity<>(new CustomerErrorResponse("success",
                    "Customer saved successfully"), HttpStatus.CREATED);
        } catch (IOException e) {
            logger.error("Error processing profile picture: {}", e.getMessage(), e);
            return new ResponseEntity<>(new CustomerErrorResponse("error",
                    "Error processing profile picture"), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (DataPersistFailedException e) {
            logger.error("Failed to save customer: {}", e.getMessage());
            return new ResponseEntity<>(new CustomerErrorResponse("error",
                    "Failed to save customer"), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Unexpected error while saving customer: {}", e.getMessage(), e);
            return new ResponseEntity<>(new CustomerErrorResponse("error",
                    "Unexpected error occurred"), HttpStatus.INTERNAL_SERVER_ERROR);
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
