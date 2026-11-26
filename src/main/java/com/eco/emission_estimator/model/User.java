package com.eco.emission_estimator.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private Integer familyMembers = 1;
    private String householdType = "Apartment";
    private String city = "Bengaluru";

    public User() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Integer getFamilyMembers() { return familyMembers; }
    public void setFamilyMembers(Integer familyMembers) { this.familyMembers = familyMembers; }

    public String getHouseholdType() { return householdType; }
    public void setHouseholdType(String householdType) { this.householdType = householdType; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
}