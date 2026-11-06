package com.pe.pivot.credit.domain.port.out;

import com.pe.pivot.credit.domain.model.CustomerSummary;
import io.reactivex.rxjava3.core.Maybe;

public interface CustomerClientPort {
  Maybe<CustomerSummary> findCustomerById(String customerId);
}