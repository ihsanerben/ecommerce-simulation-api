package com.ihsanerben.ecommerce_simulation_api.exception.message;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ErrorMessageSeeder implements ApplicationRunner {

    private final ErrorMessageRepository errorMessageRepository;

    @Override
    public void run(ApplicationArguments args) {
        int insertedCount = 0;
        for (ResolvedErrorMessage message : DefaultErrorMessages.all()) {
            if (errorMessageRepository.findByCode(message.code()).isEmpty()) {
                errorMessageRepository.save(ErrorMessageDocument.builder()
                        .code(message.code())
                        .message(message.message())
                        .build());
                insertedCount++;
            }
        }
        log.info("event=error_messages_seeded insertedCount={} totalDefaults={}",
                insertedCount, DefaultErrorMessages.all().size());
    }
}
