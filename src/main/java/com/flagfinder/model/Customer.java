package com.flagfinder.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.SQLDelete;

@Data
@Entity
@Table(name = "customers")
@SQLDelete(sql = "UPDATE customers SET deleted = true WHERE id=?")
@FilterDef(name = "deletedCustomerFilter", parameters = @ParamDef(name = "isDeleted", type = Boolean.class))
@Filter(name = "deletedCustomerFilter", condition = "deleted = :isDeleted")
public class Customer extends BaseEntity<Long> {
    /**
     * The customer's firstname.
     */
    @Column
    private String firstname;

    /**
     * The customer's lastname.
     */
    @Column
    private String lastname;

    /**
     * The customer's address.
     */
    @Column
    private String address;

    /**
     * The customer's email.
     */
    @Size(max = 320)
    private String email;

    /**
     * The customer's phone number.
     */
    @Column(unique = true)
    private String phoneNumber;
}
