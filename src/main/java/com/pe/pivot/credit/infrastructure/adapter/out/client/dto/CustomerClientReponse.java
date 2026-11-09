package com.pe.pivot.credit.infrastructure.adapter.out.client.dto;

import lombok.Builder;

/**
 * Immutable DTO representing the JSON response from ms-customer.
 */
@Builder
public record CustomerClientReponse(
        String id,
        String customerType,
        String profile,
        String documentType,
        String documentNumber,
        String status
) {
}
