package com.warsha.erp.services;

import com.warsha.erp.entities.ShippingZone;
import com.warsha.erp.repository.ShippingZoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ShippingZoneService {

    @Autowired
    private ShippingZoneRepository shippingZoneRepository;

    public List<ShippingZone> getAllShippingZones() {
        return shippingZoneRepository.findAll();
    }

    public Optional<ShippingZone> getShippingZoneById(Integer id) {
        return shippingZoneRepository.findById(id);
    }

    public ShippingZone saveShippingZone(ShippingZone shippingZone) {
        return shippingZoneRepository.save(shippingZone);
    }
    
    public void deleteShippingZone(Integer id) {
        shippingZoneRepository.deleteById(id);
    }
    
    public ShippingZone updateShippingPrice(Integer id, BigDecimal newPrice) {
        Optional<ShippingZone> optionalZone = shippingZoneRepository.findById(id);
        if (optionalZone.isPresent()) {
            ShippingZone zone = optionalZone.get();
            zone.setShippingFee(newPrice);
            return shippingZoneRepository.save(zone);
        }
        throw new RuntimeException("Shipping Zone not found with id: " + id);
    }
}