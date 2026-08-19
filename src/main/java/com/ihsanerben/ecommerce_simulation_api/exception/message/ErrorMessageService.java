package com.ihsanerben.ecommerce_simulation_api.exception.message;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ErrorMessageService {

    private final ErrorMessageRepository errorMessageRepository;

    public ResolvedErrorMessage resolve(String code) {
        ErrorMessageDocument document = errorMessageRepository.findByCode(code)
                .filter(errorMessage -> errorMessage.getCode() != null && !errorMessage.getCode().isBlank())
                .filter(errorMessage -> errorMessage.getMessage() != null && !errorMessage.getMessage().isBlank())
                .orElseThrow(() -> new IllegalStateException("MongoDB error message is missing for code: " + code));

        return new ResolvedErrorMessage(document.getCode(), document.getMessage());
    }
}
