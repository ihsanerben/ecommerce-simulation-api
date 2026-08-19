package com.ihsanerben.ecommerce_simulation_api.exception.message;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ErrorMessageRepository extends MongoRepository<ErrorMessageDocument, String> {

    Optional<ErrorMessageDocument> findByCode(String code);
}
