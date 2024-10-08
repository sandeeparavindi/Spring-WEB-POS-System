package org.example.springwebpos.controller;

import lombok.RequiredArgsConstructor;
import org.example.springwebpos.dto.CustomerDTO;
import org.example.springwebpos.exception.CustomerNotFoundException;
import org.example.springwebpos.exception.DataPersistFailedException;
import org.example.springwebpos.service.CustomerService;
import org.example.springwebpos.util.AppUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> saveCustomer(
            @RequestPart("name") String name,
            @RequestPart("address") String address,
            @RequestPart("mobile") String mobile,
            @RequestPart("profilePic") MultipartFile profilePic) {

        try {
            byte[] imageBytes = profilePic.getBytes();
            String base64ProfilePic = AppUtil.toBase64ProfilePic(imageBytes);

            CustomerDTO buildCustomerDTO = new CustomerDTO();
            buildCustomerDTO.setName(name);
            buildCustomerDTO.setAddress(address);
            buildCustomerDTO.setMobile(mobile);
            buildCustomerDTO.setProfilePic(base64ProfilePic);

            customerService.saveCustomer(buildCustomerDTO);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (DataPersistFailedException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
