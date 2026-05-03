package com.erp.stock.rmi;

import com.erp.rmi.StockServiceRmi;
import com.erp.stock.service.StockService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

@Slf4j
@Service
@ConditionalOnProperty(name = "rmi.enabled", havingValue = "true", matchIfMissing = true)
public class StockServiceRmiImpl extends UnicastRemoteObject implements StockServiceRmi {

    private static final long serialVersionUID = 1L;

    private final StockService stockService;

    public StockServiceRmiImpl(StockService stockService) throws RemoteException {
        super();
        this.stockService = stockService;
        log.info("StockService RMI relié au service métier StockService");
    }

    @Override
    public Integer getProductQuantity(Long productId) throws RemoteException {
        try {
            return stockService.findById(productId).getQuantiteEnStock();
        } catch (EntityNotFoundException e) {
            throw new RemoteException(e.getMessage(), e);
        } catch (Exception e) {
            log.error("RMI Stock getProductQuantity", e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public Boolean isProductAvailable(Long productId, Integer quantity) throws RemoteException {
        try {
            if (quantity == null || quantity <= 0) {
                return false;
            }
            var p = stockService.findById(productId);
            return p.getQuantiteEnStock() >= quantity;
        } catch (EntityNotFoundException e) {
            return false;
        } catch (Exception e) {
            log.error("RMI Stock isProductAvailable", e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public Boolean reserveProduct(Long productId, Integer quantity) throws RemoteException {
        try {
            if (quantity == null || quantity <= 0) {
                return false;
            }
            stockService.sortieStock(productId, quantity);
            return true;
        } catch (IllegalStateException e) {
            log.warn("RMI Stock réservation refusée: {}", e.getMessage());
            return false;
        } catch (EntityNotFoundException e) {
            throw new RemoteException(e.getMessage(), e);
        } catch (Exception e) {
            log.error("RMI Stock reserveProduct", e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public String getProductName(Long productId) throws RemoteException {
        try {
            return stockService.findById(productId).getNom();
        } catch (EntityNotFoundException e) {
            throw new RemoteException(e.getMessage(), e);
        } catch (Exception e) {
            log.error("RMI Stock getProductName", e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public Double getProductPrice(Long productId) throws RemoteException {
        try {
            return stockService.findById(productId).getPrixUnitaire().doubleValue();
        } catch (EntityNotFoundException e) {
            throw new RemoteException(e.getMessage(), e);
        } catch (Exception e) {
            log.error("RMI Stock getProductPrice", e);
            throw new RemoteException(e.getMessage(), e);
        }
    }
}
