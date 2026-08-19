const errorMessages = [
  { code: "RESOURCE_NOT_FOUND", message: "The requested resource was not found." },
  { code: "DUPLICATE_RESOURCE", message: "The resource already exists." },
  { code: "INSUFFICIENT_STOCK", message: "There is not enough stock for this product." },
  { code: "EMPTY_CART", message: "Cart is empty, cannot checkout." },
  { code: "INVALID_ORDER_STATE", message: "The order is not in a valid state for this operation." },
  { code: "INVALID_TOKEN", message: "The token is invalid or expired." },
  { code: "PASSWORD_REUSE", message: "The new password cannot be a recently used password." },
  { code: "RATE_LIMIT_EXCEEDED", message: "Too many requests. Please try again later." },
  { code: "MALFORMED_REQUEST_BODY", message: "Malformed request body." },
  { code: "TYPE_MISMATCH", message: "The request parameter has an invalid value." },
  { code: "INVALID_SORT_PROPERTY", message: "The requested sort property is invalid." },
  { code: "MISSING_PARAMETER", message: "A required request parameter is missing." },
  { code: "METHOD_NOT_SUPPORTED", message: "The HTTP method is not supported for this endpoint." },
  { code: "MEDIA_TYPE_NOT_SUPPORTED", message: "The request content type is not supported." },
  { code: "ENDPOINT_NOT_FOUND", message: "The requested endpoint was not found." },
  { code: "DATA_INTEGRITY_VIOLATION", message: "The operation conflicts with existing or related data." },
  { code: "MAIL_FAILURE", message: "Email service is temporarily unavailable. Please try again later." },
  { code: "BAD_CREDENTIALS", message: "Invalid username or password." },
  { code: "ACCESS_DENIED", message: "You do not have permission to perform this action." },
  { code: "VALIDATION_FAILED", message: "Validation failed." },
  { code: "UNEXPECTED_ERROR", message: "An unexpected error occurred." },
  { code: "AUTHENTICATION_REQUIRED", message: "Authentication is required to access this resource." },
  { code: "CSRF_TOKEN_INVALID", message: "CSRF token is missing or invalid. Reload Swagger UI or initialize CSRF protection and retry." }
];

db.error_messages.createIndex({ code: 1 }, { unique: true });

errorMessages.forEach((errorMessage) => {
  db.error_messages.updateOne(
    { code: errorMessage.code },
    { $set: errorMessage },
    { upsert: true }
  );
});

print(`${errorMessages.length} error messages inserted or updated.`);
