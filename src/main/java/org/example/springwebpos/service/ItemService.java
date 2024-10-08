package org.example.springwebpos.service;

import org.example.springwebpos.customObj.CustomerResponse;
import org.example.springwebpos.dto.CustomerDTO;
import org.example.springwebpos.dto.ItemDTO;

import java.util.List;

public interface ItemService {
    void saveItem(ItemDTO itemDTO);
    void updateItem(ItemDTO itemDTO);
    void deleteItem(String code);
    CustomerResponse getSelectedItem(String code);
    List<ItemDTO> getAllItems();
}
