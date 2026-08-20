package com.ihsanerben.ecommerce_simulation_api.exception.message;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class ErrorMessageSeederTest {

    private final ErrorMessageRepository repository = mock(ErrorMessageRepository.class);
    private final ErrorMessageSeeder seeder = new ErrorMessageSeeder(repository);

    @Test
    void run_whenMessagesAreMissing_insertsAllDefaults() throws Exception {
        given(repository.findByCode(org.mockito.ArgumentMatchers.anyString()))
                .willReturn(Optional.empty());

        seeder.run(null);

        ArgumentCaptor<ErrorMessageDocument> captor = ArgumentCaptor.forClass(ErrorMessageDocument.class);
        verify(repository, org.mockito.Mockito.times(DefaultErrorMessages.all().size())).save(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(ErrorMessageDocument::getCode)
                .contains(ErrorMessageCodes.AUTHENTICATION_REQUIRED,
                        ErrorMessageCodes.CONVERSATION_ALREADY_ASSIGNED);
    }

    @Test
    void run_whenMessageAlreadyExists_doesNotOverwriteIt() throws Exception {
        given(repository.findByCode(org.mockito.ArgumentMatchers.anyString()))
                .willReturn(Optional.of(ErrorMessageDocument.builder()
                        .code("CUSTOM")
                        .message("Custom message")
                        .build()));

        seeder.run(null);

        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
