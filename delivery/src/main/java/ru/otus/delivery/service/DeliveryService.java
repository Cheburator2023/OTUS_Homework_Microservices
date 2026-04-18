package ru.otus.delivery.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.delivery.dto.ReleaseDeliveryRequest;
import ru.otus.delivery.dto.ReserveDeliveryRequest;
import ru.otus.delivery.dto.DeliveryResponse;
import ru.otus.delivery.exception.DeliverySlotNotAvailableException;
import ru.otus.delivery.exception.DeliverySlotNotFoundException;
import ru.otus.delivery.model.DeliverySlot;
import ru.otus.delivery.repository.DeliverySlotRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliverySlotRepository slotRepository;

    @Transactional
    public DeliveryResponse reserve(ReserveDeliveryRequest request) {
        // Получаем все слоты на указанное время
        List<DeliverySlot> slots = slotRepository.findByTimeSlot(request.deliveryTime());

        if (slots.isEmpty()) {
            throw new DeliverySlotNotFoundException("No slot found for time: " + request.deliveryTime());
        }

        // Ищем первый свободный слот (не зарезервированный)
        DeliverySlot freeSlot = slots.stream()
                .filter(slot -> !slot.isReserved())
                .findFirst()
                .orElseThrow(() -> new DeliverySlotNotAvailableException(
                        "All slots are already reserved for time: " + request.deliveryTime()));

        // Резервируем выбранный слот
        freeSlot.setOrderId(request.orderId());
        freeSlot.setReserved(true);
        slotRepository.save(freeSlot);

        log.info("Reserved delivery slot {} (courier {}) for order {}",
                request.deliveryTime(), freeSlot.getCourierId(), request.orderId());
        return new DeliveryResponse(true, "Delivery slot reserved");
    }

    @Transactional
    public void release(ReleaseDeliveryRequest request) {
        DeliverySlot slot = slotRepository.findByOrderId(request.orderId())
                .orElseThrow(() -> new DeliverySlotNotFoundException("No reservation found for order: " + request.orderId()));

        slot.setOrderId(null);
        slot.setReserved(false);
        slotRepository.save(slot);
        log.info("Released delivery slot for order {}", request.orderId());
    }
}