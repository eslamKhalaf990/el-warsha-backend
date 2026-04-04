package com.warsha.erp.controllers;

import com.warsha.erp.entities.ShippingZone;
import com.warsha.erp.services.ShippingZoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/shipping")
public class ShippingZoneController {

    @Autowired
    private ShippingZoneService shippingZoneService;

    @GetMapping
    public List<ShippingZone> getAll() {
        return shippingZoneService.getAllShippingZones();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShippingZone> getById(@PathVariable Integer id) {
        Optional<ShippingZone> shippingZone = shippingZoneService.getShippingZoneById(id);
        return shippingZone.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ShippingZone create(@RequestBody ShippingZone shippingZone) {
        return shippingZoneService.saveShippingZone(shippingZone);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        shippingZoneService.deleteShippingZone(id);
    }

    @PutMapping("/{id}")
    public ShippingZone updatePrice(@PathVariable Integer id, @RequestParam BigDecimal shippingFee) {
        return shippingZoneService.updateShippingPrice(id, shippingFee);
    }
}