package com.pe.pivot.credit.infrastructure.adapter.in.web;

import com.pe.pivot.credit.domain.port.in.*;
import com.pe.pivot.credit.infrastructure.adapter.in.web.dto.CreditBalanceUpdateRequest;
import com.pe.pivot.credit.infrastructure.adapter.in.web.dto.CreditBalanceResponse;
import com.pe.pivot.credit.infrastructure.adapter.in.web.dto.CreditRequest;
import com.pe.pivot.credit.infrastructure.adapter.in.web.dto.CreditResponse;
import com.pe.pivot.credit.infrastructure.adapter.in.web.mapper.CreditWebMapper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller implementing the OpenAPI contract for Active Credit Product operations with RxJava 3.
 */
@RestController
@RequestMapping("/api/v1/credits")
@RequiredArgsConstructor
public class CreditController {

  private final CreateCreditUseCase createCreditUseCase;
  private final RetrieveCreditUseCase retrieveCreditUseCase;
  private final UpdateCreditUseCase updateCreditUseCase;
  private final UpdateCreditBalanceUseCase updateCreditBalanceUseCase;
  private final DeleteCreditUseCase deleteCreditUseCase;
  private final CreditWebMapper mapper;

  /**
   * Grant a new loan or issue a credit card (returns unmasked number once).
   * @POST /api/v1/credits
   * @param request - CreditRequest DTO containing credit product details.
   * @return - Single emitting CreditResponse with unmasked credit number.
   */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Single<CreditResponse> createCredit(@Valid @RequestBody CreditRequest request) {
    return createCreditUseCase.create(mapper.toDomain(request))
            .map(mapper::toUnmaskedResponse);
  }

  /**
   * Retrieve all credit products (masked numbers).
   * @GET /api/v1/credits
   * @return - Flowable emitting CreditResponse objects with masked credit numbers.
   */
  @GetMapping
  public Flowable<CreditResponse> getAllCredits() {
    return retrieveCreditUseCase.findAll()
            .map(mapper::toResponse);
  }

  /**
   * Retrieve a credit product by its ID (masked number).
   * @GET /api/v1/credits/{id}
   * @param id - the unique identifier of the credit product.
   * @return - Single emitting CreditResponse with masked credit number.
   */
  @GetMapping("/{id}")
  public Single<CreditResponse> getCreditById(@PathVariable String id) {
    return retrieveCreditUseCase.findById(id)
            .map(mapper::toResponse);
  }

  /**
   * Retrieve a credit product by its plain number via SHA-256 hash.
   * @GET /api/v1/credits/number/{creditNumber}
   * @param creditNumber - the plain credit number to look up.
   * @return - Single emitting CreditResponse with masked credit number.
   */
  @GetMapping("/number/{creditNumber}")
  public Single<CreditResponse> getCreditByNumber(@PathVariable String creditNumber) {
    return retrieveCreditUseCase.findByCreditNumber(creditNumber)
            .map(mapper::toResponse);
  }

  /**
   * Retrieve all credit products belonging to a customer.
   * @GET /api/v1/credits/customer/{customerId}
   * @param customerId - the unique identifier of the customer.
   * @return - Flowable emitting CreditResponse objects for the customer's credits.
   */
  @GetMapping("/customer/{customerId}")
  public Flowable<CreditResponse> getCreditsByCustomerId(@PathVariable String customerId) {
    return retrieveCreditUseCase.findByCustomerId(customerId)
            .map(mapper::toResponse);
  }

  /**
   * Consult available credit limit and outstanding debt.
   * @GET /api/v1/credits/{id}/balance
   * @param id - the unique identifier of the credit product.
   * @return - Single emitting CreditBalanceResponse with masked credit number and balance details.
   */
  @GetMapping("/{id}/balance")
  public Single<CreditBalanceResponse> getCreditBalance(@PathVariable String id) {
    return retrieveCreditUseCase.findById(id)
            .map(mapper::toBalanceResponse);
  }

  /**
   * Process a credit PAYMENT or credit card CONSUMPTION.
   * @PATCH /api/v1/credits/{id}/balance
   * @param id - the unique identifier of the credit product.
   * @param request - CreditBalanceUpdateRequest DTO containing operation type and amount.
   * @return - Single emitting updated CreditResponse with masked credit number.
   */
  @PatchMapping("/{id}/balance")
  public Single<CreditResponse> updateCreditBalance(@PathVariable String id, @Valid @RequestBody CreditBalanceUpdateRequest request) {
    return updateCreditBalanceUseCase.updateBalance(id, request.operationType(), request.amount())
            .map(mapper::toResponse);
  }

  /**
   * Update administrative terms of an existing credit product.
   * @PUT /api/v1/credits/{id}
   * @param id - the unique identifier of the credit product to update.
   * @param request - CreditRequest DTO containing new values for administrative terms.
   * @return - Single emitting updated CreditResponse with masked credit number.
   */
  @PutMapping("/{id}")
  public Single<CreditResponse> updateCredit(@PathVariable String id, @Valid @RequestBody CreditRequest request) {
    return updateCreditUseCase.updateCredit(id, mapper.toDomain(request))
            .map(mapper::toResponse);
  }

  /**
   * Soft delete (close) a credit product when usedAmount == 0.00.
   * @DELETE /api/v1/credits/{id}
   * @param id - the unique identifier of the credit product to logically close.
   * @return - Completable that completes when the credit product is successfully logically closed, or emits an error if the operation fails.
   */
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Completable deleteCredit(@PathVariable String id) {
    return deleteCreditUseCase.delete(id);
  }
}
