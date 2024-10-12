package org.example.springwebpos.dao;

import org.example.springwebpos.entity.OrderDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OderDetailDAO extends JpaRepository<OrderDetailEntity, Long> {
}
