package org.example.springwebpos.controller;

import lombok.RequiredArgsConstructor;
import org.example.springwebpos.customObj.ItemErrorResponse;
import org.example.springwebpos.customObj.ItemResponse;
import org.example.springwebpos.dto.ItemDTO;
import org.example.springwebpos.exception.DataPersistFailedException;
import org.example.springwebpos.exception.ItemNotFoundException;
import org.example.springwebpos.service.ItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/items")
@RequiredArgsConstructor
public class ItemController {

    private static final Logger logger = LoggerFactory.getLogger(ItemController.class);

    @Autowired
    private ItemService itemService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createItem(@RequestBody ItemDTO item) {
        if (item == null || item.getDescription() == null || item.getPrice() <= 0 || item.getQty() < 0) {
            logger.warn("Invalid item data: {}", item);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        try {
            itemService.saveItem(item);
            logger.info("Item created successfully: {}", item);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (DataPersistFailedException e) {
            logger.error("Data persistence failed: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Internal server error: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping(value = "/{code}",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateNote(@PathVariable ("code") String itemCode, @RequestBody ItemDTO item) {
        try {
            if (item == null || itemCode == null || itemCode.isEmpty()) {
                logger.warn("Invalid update request for itemCode: {}", itemCode);
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            itemService.updateItem(itemCode, item);
            logger.info("Item updated successfully: {}", itemCode);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (ItemNotFoundException e) {
            logger.error("Item not found: {}", itemCode);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Internal server error: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<Void> deleteItem(@PathVariable("code") String code) {
        try {
            itemService.deleteItem(code);
            logger.info("Item deleted successfully: {}", code);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (ItemNotFoundException e) {
            logger.error("Item not found: {}", code);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Internal server error: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ItemResponse getSelectedItem(@PathVariable ("code") String code)  {
        ItemResponse itemResponse = itemService.getSelectedItem(code);
        if (itemResponse instanceof ItemErrorResponse) {
            logger.warn("Item not found: {}", code);
        } else {
            logger.info("Fetched item details for: {}", code);
        }
        return itemResponse;
    }

    @GetMapping(value = "allitems", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ItemDTO> getAllItems() {
        logger.info("Fetching all items");
        return itemService.getAllItems();
    }

}
