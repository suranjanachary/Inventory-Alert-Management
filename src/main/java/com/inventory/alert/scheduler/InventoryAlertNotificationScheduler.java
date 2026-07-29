package com.inventory.alert.scheduler;

import com.inventory.alert.dto.response.InventoryAlertResponse;
import com.inventory.alert.service.InventoryAlertService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Sends mock email notifications for PENDING low-stock alerts.
 * Kept outside controllers/services so scheduling policy can change without touching domain rules.
 *
 * Cron {@code 0 0 9 * * *} = second=0, minute=0, hour=9, every day/month, any weekday → 09:00:00 daily.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class InventoryAlertNotificationScheduler {

    private static final int BATCH_SIZE = 50;

    private final InventoryAlertService inventoryAlertService;

    @Value("${app.scheduler.alert-cron}")
    private String alertCron;

    @Scheduled(cron = "${app.scheduler.alert-cron}")
    public void dispatchPendingAlerts() {
        long started = System.currentTimeMillis();
        int processed = 0;
        int skipped = 0;
        int failed = 0;

        log.info("Alert notification scheduler started cron={}", alertCron);
        try {
            Page<InventoryAlertResponse> page =
                    inventoryAlertService.findPendingAlertsForDispatch(PageRequest.of(0, BATCH_SIZE));
            for (InventoryAlertResponse alert : page.getContent()) {
                try {
                    mockEmail(alert);
                    Optional<InventoryAlertResponse> sent = inventoryAlertService.markAlertSent(alert.getId());
                    if (sent.isPresent()) {
                        processed++;
                    } else {
                        skipped++;
                        log.debug("Alert {} already processed; skipping", alert.getId());
                    }
                } catch (Exception ex) {
                    failed++;
                    log.error("Failed to notify alert id={}", alert.getId(), ex);
                }
            }
        } catch (Exception ex) {
            log.error("Alert notification scheduler failed", ex);
        } finally {
            long elapsed = System.currentTimeMillis() - started;
            log.info(
                    "Alert notification scheduler finished processed={} skipped={} failed={} elapsedMs={}",
                    processed,
                    skipped,
                    failed,
                    elapsed);
        }
    }

    private void mockEmail(InventoryAlertResponse alert) {
        log.info(
                """
                ===== MOCK EMAIL =====
                To: ops@inventory.local
                Subject: LOW_STOCK alert #{}
                Body: productId={} sku={} status={} message={}
                ======================
                """,
                alert.getId(),
                alert.getProductId(),
                alert.getProductSku(),
                alert.getStatus(),
                alert.getMessage());
    }
}
