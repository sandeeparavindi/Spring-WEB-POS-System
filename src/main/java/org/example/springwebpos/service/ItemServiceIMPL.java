package org.example.springwebpos.service;


import lombok.RequiredArgsConstructor;
import org.example.springwebpos.customObj.CustomerResponse;
import org.example.springwebpos.dao.ItemDAO;
import org.example.springwebpos.dto.ItemDTO;

import org.example.springwebpos.entity.ItemEntity;
import org.example.springwebpos.exception.DataPersistFailedException;
import org.example.springwebpos.exception.ItemNotFoundException;
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
public class ItemServiceIMPL implements ItemService {
    @Autowired
    private final ItemDAO itemDAO;

    @Autowired
    private final Mapping mapping;

    @Override
    public void saveItem(ItemDTO itemDTO) {
        itemDTO.setCode(AppUtil.createItemCode());
        try {
            ItemEntity itemEntity = mapping.convertToItemEntity(itemDTO);
            ItemEntity savedItem = itemDAO.save(itemEntity);

            if (savedItem == null) {
                throw new DataPersistFailedException("Failed to save item data");
            }
        } catch (Exception e) {
            System.err.println("Error saving item: " + e.getMessage());
            throw new DataPersistFailedException("Cannot save item data: " + e.getMessage());
        }
    }

    @Override
    public void updateItem(String code, ItemDTO itemDTO) {
        Optional<ItemEntity> tmpItemEntity= itemDAO.findById(code);
        if(!tmpItemEntity.isPresent()){
            throw new ItemNotFoundException("Item not found");
        }else {
            tmpItemEntity.get().setDescription(itemDTO.getDescription());
            tmpItemEntity.get().setPrice(itemDTO.getPrice());
            tmpItemEntity.get().setQty(itemDTO.getQty());
        }
    }

    @Override
    public void deleteItem(String code) {

    }

    @Override
    public CustomerResponse getSelectedItem(String code) {
        return null;
    }

    @Override
    public List<ItemDTO> getAllItems() {
        return List.of();
    }
}
