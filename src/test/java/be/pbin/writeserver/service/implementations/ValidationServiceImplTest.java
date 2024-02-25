package be.pbin.writeserver.service.implementations;

import be.pbin.writeserver.data.payload.Payload;
import be.pbin.writeserver.data.payload.validation.PayloadValidationException;
import be.pbin.writeserver.data.payload.validation.PayloadValidator;
import be.pbin.writeserver.data.payload.validation.ValidationResult;
import be.pbin.writeserver.data.payload.validation.ValidationStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValidationServiceImplTest {

    @Test
    void test_noErrors() throws PayloadValidationException {
        Payload payload = Payload.builder().build();
        ValidationResult validationResult = new ValidationResult(ValidationStatus.VALIDATED, new ArrayList<>());

        PayloadValidator payloadValidator1 = mock(PayloadValidator.class);
        when(payloadValidator1.validate(payload)).thenReturn(validationResult);
        ValidationServiceImpl validationService = new ValidationServiceImpl(List.of(payloadValidator1));

        validationService.validate(payload);

        verify(payloadValidator1, times(1)).validate(payload);
    }

    @Test
    void test_validationErrors() {
        Payload payload = Payload.builder().build();

        String error1 = "the first error in validating the payload";
        String error2 = "the second error in validating the payload";
        ValidationResult validationResult1 = new ValidationResult(ValidationStatus.INVALID, List.of(error1));
        ValidationResult validationResult2 = new ValidationResult(ValidationStatus.VALIDATED, new ArrayList<>());
        ValidationResult validationResult3 = new ValidationResult(ValidationStatus.INVALID, List.of(error2));

        PayloadValidator payloadValidator1 = mock(PayloadValidator.class);
        PayloadValidator payloadValidator2 = mock(PayloadValidator.class);
        PayloadValidator payloadValidator3 = mock(PayloadValidator.class);
        when(payloadValidator1.validate(payload)).thenReturn(validationResult1);
        when(payloadValidator2.validate(payload)).thenReturn(validationResult2);
        when(payloadValidator3.validate(payload)).thenReturn(validationResult3);
        ValidationServiceImpl validationService = new ValidationServiceImpl(
                List.of(payloadValidator1, payloadValidator2, payloadValidator3));

        PayloadValidationException thrown = assertThrows(PayloadValidationException.class,
                () -> validationService.validate(payload));

        assertThat(thrown.getMessage())
                .isEqualTo("Payload validation errors encountered: "
                        + String.join(" :: ", error1, error2));

        verify(payloadValidator1, times(1)).validate(payload);
        verify(payloadValidator2, times(1)).validate(payload);
        verify(payloadValidator3, times(1)).validate(payload);
    }

}