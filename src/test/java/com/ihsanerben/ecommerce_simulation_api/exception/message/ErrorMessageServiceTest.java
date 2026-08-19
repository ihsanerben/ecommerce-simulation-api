package com.ihsanerben.ecommerce_simulation_api.exception.message;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ErrorMessageServiceTest {

    @Mock
    private ErrorMessageRepository errorMessageRepository;

    @InjectMocks
    private ErrorMessageService errorMessageService;

    @Test
    void resolve_whenCodeExists_returnsMongoCodeAndMessage() {
        ErrorMessageDocument document = ErrorMessageDocument.builder()
                .code(ErrorMessageCodes.RESOURCE_NOT_FOUND)
                .message("MongoDB message")
                .build();
        given(errorMessageRepository.findByCode(ErrorMessageCodes.RESOURCE_NOT_FOUND))
                .willReturn(Optional.of(document));

        ResolvedErrorMessage errorMessage = errorMessageService.resolve(ErrorMessageCodes.RESOURCE_NOT_FOUND);

        assertThat(errorMessage.code()).isEqualTo(ErrorMessageCodes.RESOURCE_NOT_FOUND);
        assertThat(errorMessage.message()).isEqualTo("MongoDB message");
    }

    @Test
    void resolve_whenCodeDoesNotExist_throwsException() {
        given(errorMessageRepository.findByCode(ErrorMessageCodes.RESOURCE_NOT_FOUND))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> errorMessageService.resolve(ErrorMessageCodes.RESOURCE_NOT_FOUND))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void resolve_whenMongoIsUnavailable_propagatesDatabaseException() {
        given(errorMessageRepository.findByCode(ErrorMessageCodes.RESOURCE_NOT_FOUND))
                .willThrow(new DataAccessResourceFailureException("MongoDB unavailable"));

        assertThatThrownBy(() -> errorMessageService.resolve(ErrorMessageCodes.RESOURCE_NOT_FOUND))
                .isInstanceOf(DataAccessResourceFailureException.class);
    }
}
