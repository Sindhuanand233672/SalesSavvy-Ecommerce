
package com.salessavvy.repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.salessavvy.entity.Product;

import jakarta.persistence.criteria.Predicate;

public class ProductSpecification {

    public static Specification<Product> filterProducts(
            String keyword,
            Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice) {

        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            // Only active products
            predicates.add(
                    criteriaBuilder.isTrue(root.get("active"))
            );

            // Only products belonging to active categories
            predicates.add(
                    criteriaBuilder.isTrue(
                            root.join("category").get("active")
                    )
            );

            // Search by name, brand, SKU or description
            if (keyword != null && !keyword.trim().isEmpty()) {

                String search = "%" + keyword.trim().toLowerCase() + "%";

                Predicate nameMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        search
                );

                Predicate brandMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(
                                criteriaBuilder.coalesce(
                                        root.get("brand"), ""
                                )
                        ),
                        search
                );

                Predicate skuMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("sku")),
                        search
                );

                Predicate descriptionMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(
                                criteriaBuilder.coalesce(
                                        root.get("description"), ""
                                )
                        ),
                        search
                );

                predicates.add(
                        criteriaBuilder.or(
                                nameMatch,
                                brandMatch,
                                skuMatch,
                                descriptionMatch
                        )
                );
            }

            // Filter by category
            if (categoryId != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("category").get("id"),
                                categoryId
                        )
                );
            }

            // Minimum price
            if (minPrice != null) {
                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(
                                root.get("price"),
                                minPrice
                        )
                );
            }

            // Maximum price
            if (maxPrice != null) {
                predicates.add(
                        criteriaBuilder.lessThanOrEqualTo(
                                root.get("price"),
                                maxPrice
                        )
                );
            }

            return criteriaBuilder.and(
                    predicates.toArray(new Predicate[0])
            );
        };
    }
}
