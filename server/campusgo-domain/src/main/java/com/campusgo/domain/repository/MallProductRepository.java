package com.campusgo.domain.repository;

import com.campusgo.domain.model.MallProduct;

import java.util.List;
import java.util.Optional;

public interface MallProductRepository {

    List<MallProduct> findAllEnabled();

    Optional<MallProduct> findById(long id);

    boolean deductStock(long id, int delta);
}