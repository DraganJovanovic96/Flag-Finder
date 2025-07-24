package com.flagfinder.service.impl;

import com.mss.model.*;
import com.mss.repository.*;
import com.mss.service.PermanentDeletionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The {@code PermanentlyDeleteServiceImpl} class implements the {@link PermanentDeletionService} interface,
 * providing functionality to permanently delete softly deleted resources from the database.
 * Dependency injection is used to obtain instances of the necessary repository beans for handling
 * different entity types (Vehicle, User, Customer, Service, and ServiceType).
 * <p>
 * This service runs a scheduled job to periodically remove all resources marked as deleted.
 * </p>
 *
 * @author Dragan Jovanovic
 * @version 1.0
 * @since 1.0
 */
@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class PermanentlyDeleteServiceImpl implements PermanentDeletionService {
    /**
     * The repository used to retrieve vehicle data.
     */
    private final VehicleRepository vehicleRepository;

    /**
     * The repository used to retrieve token data.
     */
    private final TokenRepository tokenRepository;

    /**
     * The repository used to retrieve user data.
     */
    private final UserRepository userRepository;

    /**
     * The repository used to retrieve customer data.
     */
    private final CustomerRepository customerRepository;

    /**
     * The repository used to retrieve service data.
     */
    private final ServiceRepository serviceRepository;

    /**
     * The repository used to retrieve service type data.
     */
    private final ServiceTypeRepository serviceTypeRepository;

    /**
     * Permanently deletes resources (customers, vehicles, services, service types, and tokens) that have been deleted for
     * longer than one week. This method is scheduled to run periodically to ensure resources are permanently removed after
     * the retention period.
     *
     * <p>The resources are identified based on their deletion status and the deletion timestamp. The following operations
     * are performed:
     * <ul>
     *     <li>Find customers, vehicles, services, and service types deleted for over one week.</li>
     *     <li>Permanently delete these entities from the respective repositories by ID.</li>
     *     <li>Delete all tokens from the token repository.</li>
     * </ul>
     * </p>
     *
     * <p>This method is scheduled to run every minute, but can be adjusted for testing purposes. The method utilizes
     * repository queries that filter entities based on the deletion timestamp.</p>
     *
     * @see CustomerRepository#findCustomersDeletedOlderThanOneWeek(Instant) (LocalDateTime)
     * @see VehicleRepository#findVehiclesDeletedOlderThanOneWeek(Instant) (LocalDateTime)
     * @see ServiceRepository#findServicesDeletedOlderThanOneWeek(Instant) (LocalDateTime)
     * @see ServiceTypeRepository#findServiceTypesDeletedOlderThanOneWeek(Instant) (LocalDateTime)
     */
    @Override
    @Transactional
    @Scheduled(cron = "0 0 * * * *", zone = "GMT+2")
    public void permanentlyDeleteResources() {
        Instant oneWeekAgo = Instant.now().minus(Duration.ofDays(7));

        List<Long> deletedCustomerIds = customerRepository.findCustomersDeletedOlderThanOneWeek(oneWeekAgo)
                .stream()
                .map(Customer::getId)
                .collect(Collectors.toList());

        List<Long> deletedVehicleIds = vehicleRepository.findVehiclesDeletedOlderThanOneWeek(oneWeekAgo)
                .stream()
                .map(Vehicle::getId)
                .collect(Collectors.toList());

        List<Long> deletedServiceIds = serviceRepository.findServicesDeletedOlderThanOneWeek(oneWeekAgo)
                .stream()
                .map(Service::getId)
                .collect(Collectors.toList());

        List<Long> deletedServiceTypeIds = serviceTypeRepository.findServiceTypesDeletedOlderThanOneWeek(oneWeekAgo)
                .stream()
                .map(ServiceType::getId)
                .collect(Collectors.toList());

        List<Long> deletedTokensByIds = tokenRepository.findTokensOlderThanOneWeek(oneWeekAgo)
                .stream()
                .map(Token::getId)
                .collect(Collectors.toList());

        serviceTypeRepository.permanentlyDeleteAllDeletedServiceTypes(deletedServiceTypeIds);
        serviceRepository.permanentlyDeleteAllDeletedServices(deletedServiceIds);
        vehicleRepository.permanentlyDeleteAllDeletedVehicles(deletedVehicleIds);
        customerRepository.permanentlyDeleteAllDeletedCustomers(deletedCustomerIds);
        tokenRepository.deleteByIds(deletedTokensByIds);
    }
}