package be.pbin.writeserver.data.payload.validation;

import java.util.List;

public record ValidationResult(ValidationStatus validationStatus, List<String> errors) {}
