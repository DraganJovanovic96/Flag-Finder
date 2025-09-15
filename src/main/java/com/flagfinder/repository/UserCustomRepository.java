package com.flagfinder.repository;

import com.flagfinder.dto.UserFiltersQueryDto;
import com.flagfinder.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Custom repository implementation for User entity operations.
 * Provides advanced filtering capabilities using JPA Criteria API
 * for dynamic query construction based on user filter criteria.
 */
@Data
@Repository
public class UserCustomRepository {
    /**
     * An EntityManager instance is associated with a persistence context.
     * A persistence context is a set of entity instances in which for any
     * persistent entity identity there is a unique entity instance.
     */
    private final EntityManager entityManager;

    /**
     * Retrieves a paginated list of users based on the provided filters.
     * The method constructs a dynamic query using the criteria API to filter users
     * by full name,email, address, phone number.
     * The phone number is formatted to only contain numeric digits before filtering.
     *
     * @param filters  the {@link UserFiltersQueryDto} containing the filter criteria
     *                 for users. If any field is null, it will be ignored in the query.
     * @param pageable the {@link Pageable} object containing pagination information such as
     *                 page number and page size.
     * @return a {@link Page} of {@link User} objects that match the filter criteria.
     * The page contains the list of users, pagination details, and total number of rows.
     */
    public Page<User> findFilteredUsers(UserFiltersQueryDto filters, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery cq = cb.createQuery();
        Root<User> user = cq.from(User.class);
        List<Predicate> predicates = new ArrayList<>();

        if (Objects.nonNull(filters) && Objects.nonNull(filters.getFullName())) {
            Expression<String> fullName = cb.concat(user.get("firstname"), " ");
            fullName = cb.concat(fullName, user.get("lastname"));
            predicates.add(cb.like(cb.lower(fullName), "%" + filters.getFullName().toLowerCase() + "%"));
        }

        if (Objects.nonNull(filters) && Objects.nonNull(filters.getAddress())) {
            predicates.add(cb.like(cb.lower(user.get("address")), "%" + filters.getAddress().toLowerCase() + "%"));
        }

        if (Objects.nonNull(filters) && Objects.nonNull(filters.getEmail())) {
            predicates.add(cb.like(cb.lower(user.get("email")), "%" + filters.getEmail().toLowerCase() + "%"));
        }

        if (Objects.nonNull(filters) && Objects.nonNull(filters.getPhoneNumber())) {
            filters.setPhoneNumber(filters.getPhoneNumber().replaceAll("[^0-9]", ""));
            predicates.add(cb.like(cb.lower(user.get("mobileNumber")), "%" + filters.getPhoneNumber() + "%"));
        }

        cq.where(cb.and(predicates.toArray(new Predicate[0])));
        cq.orderBy(cb.asc(user.get("firstname")));
        cq.select(user).distinct(true);

        TypedQuery<User> query = entityManager.createQuery(cq);
        int totalRows = query.getResultList().size();
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        return new PageImpl<>(query.getResultList(), pageable, totalRows);
    }
}
