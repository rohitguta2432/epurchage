package com.softage.epurchase.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.softage.epurchase.domain.PurchaseOrder;

import com.softage.epurchase.repository.PurchaseOrderRepository;
import com.softage.epurchase.repository.search.PurchaseOrderSearchRepository;
import com.softage.epurchase.web.rest.util.HeaderUtil;
import com.softage.epurchase.service.dto.PurchaseOrderDTO;
import com.softage.epurchase.service.mapper.PurchaseOrderMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing PurchaseOrder.
 */
@RestController
@RequestMapping("/api")
public class PurchaseOrderResource {

    private final Logger log = LoggerFactory.getLogger(PurchaseOrderResource.class);
        
    @Inject
    private PurchaseOrderRepository purchaseOrderRepository;

    @Inject
    private PurchaseOrderMapper purchaseOrderMapper;

    @Inject
    private PurchaseOrderSearchRepository purchaseOrderSearchRepository;

    /**
     * POST  /purchase-orders : Create a new purchaseOrder.
     *
     * @param purchaseOrderDTO the purchaseOrderDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new purchaseOrderDTO, or with status 400 (Bad Request) if the purchaseOrder has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/purchase-orders")
    @Timed
    public ResponseEntity<PurchaseOrderDTO> createPurchaseOrder(@RequestBody PurchaseOrderDTO purchaseOrderDTO) throws URISyntaxException {
        log.debug("REST request to save PurchaseOrder : {}", purchaseOrderDTO);
        if (purchaseOrderDTO.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("purchaseOrder", "idexists", "A new purchaseOrder cannot already have an ID")).body(null);
        }
        PurchaseOrder purchaseOrder = purchaseOrderMapper.purchaseOrderDTOToPurchaseOrder(purchaseOrderDTO);
        purchaseOrder = purchaseOrderRepository.save(purchaseOrder);
        PurchaseOrderDTO result = purchaseOrderMapper.purchaseOrderToPurchaseOrderDTO(purchaseOrder);
        purchaseOrderSearchRepository.save(purchaseOrder);
        return ResponseEntity.created(new URI("/api/purchase-orders/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("purchaseOrder", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /purchase-orders : Updates an existing purchaseOrder.
     *
     * @param purchaseOrderDTO the purchaseOrderDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated purchaseOrderDTO,
     * or with status 400 (Bad Request) if the purchaseOrderDTO is not valid,
     * or with status 500 (Internal Server Error) if the purchaseOrderDTO couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/purchase-orders")
    @Timed
    public ResponseEntity<PurchaseOrderDTO> updatePurchaseOrder(@RequestBody PurchaseOrderDTO purchaseOrderDTO) throws URISyntaxException {
        log.debug("REST request to update PurchaseOrder : {}", purchaseOrderDTO);
        if (purchaseOrderDTO.getId() == null) {
            return createPurchaseOrder(purchaseOrderDTO);
        }
        PurchaseOrder purchaseOrder = purchaseOrderMapper.purchaseOrderDTOToPurchaseOrder(purchaseOrderDTO);
        purchaseOrder = purchaseOrderRepository.save(purchaseOrder);
        PurchaseOrderDTO result = purchaseOrderMapper.purchaseOrderToPurchaseOrderDTO(purchaseOrder);
        purchaseOrderSearchRepository.save(purchaseOrder);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("purchaseOrder", purchaseOrderDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /purchase-orders : get all the purchaseOrders.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of purchaseOrders in body
     */
    @GetMapping("/purchase-orders")
    @Timed
    public List<PurchaseOrderDTO> getAllPurchaseOrders() {
        log.debug("REST request to get all PurchaseOrders");
        List<PurchaseOrder> purchaseOrders = purchaseOrderRepository.findAll();
        return purchaseOrderMapper.purchaseOrdersToPurchaseOrderDTOs(purchaseOrders);
    }

    /**
     * GET  /purchase-orders/:id : get the "id" purchaseOrder.
     *
     * @param id the id of the purchaseOrderDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the purchaseOrderDTO, or with status 404 (Not Found)
     */
    @GetMapping("/purchase-orders/{id}")
    @Timed
    public ResponseEntity<PurchaseOrderDTO> getPurchaseOrder(@PathVariable Long id) {
        log.debug("REST request to get PurchaseOrder : {}", id);
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findOne(id);
        PurchaseOrderDTO purchaseOrderDTO = purchaseOrderMapper.purchaseOrderToPurchaseOrderDTO(purchaseOrder);
        return Optional.ofNullable(purchaseOrderDTO)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /purchase-orders/:id : delete the "id" purchaseOrder.
     *
     * @param id the id of the purchaseOrderDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/purchase-orders/{id}")
    @Timed
    public ResponseEntity<Void> deletePurchaseOrder(@PathVariable Long id) {
        log.debug("REST request to delete PurchaseOrder : {}", id);
        purchaseOrderRepository.delete(id);
        purchaseOrderSearchRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("purchaseOrder", id.toString())).build();
    }

    /**
     * SEARCH  /_search/purchase-orders?query=:query : search for the purchaseOrder corresponding
     * to the query.
     *
     * @param query the query of the purchaseOrder search 
     * @return the result of the search
     */
    @GetMapping("/_search/purchase-orders")
    @Timed
    public List<PurchaseOrderDTO> searchPurchaseOrders(@RequestParam String query) {
        log.debug("REST request to search PurchaseOrders for query {}", query);
        return StreamSupport
            .stream(purchaseOrderSearchRepository.search(queryStringQuery(query)).spliterator(), false)
            .map(purchaseOrderMapper::purchaseOrderToPurchaseOrderDTO)
            .collect(Collectors.toList());
    }


}
