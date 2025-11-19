package com.trimminflow.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Barber Entity
 *
 * Represents a barber working at a barbershop
 * Each barber belongs to one barbershop and can provide services
 */
@Entity
@Table(name = "barbers")
public class Barber {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barbershop_id", nullable = false)
    private Barbershop barbershop;

    @Column(nullable = false)
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "First name can only contain letters, spaces, hyphens, and apostrophes")
    private String firstName;

    @Column(nullable = false)
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Last name can only contain letters, spaces, hyphens, and apostrophes")
    private String lastName;

    @Column(unique = true)
    @Email(message = "Invalid email format")
    private String email;

    @Column
    @Pattern(regexp = "^$|^[\\+]?[(]?[0-9]{1,3}[)]?[-\\s\\.]?[(]?[0-9]{1,4}[)]?[-\\s\\.]?[0-9]{1,4}[-\\s\\.]?[0-9]{1,9}$",
             message = "Please provide a valid phone number")
    private String phone;

    @Column(columnDefinition = "TEXT")
    @Size(max = 500, message = "Bio must be less than 500 characters")
    private String bio;

    @Column(name = "profile_image_url")
    @Size(max = 500, message = "Image URL must be less than 500 characters")
    private String profileImageUrl;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructors
    public Barber() {}

    public Barber(Barbershop barbershop, String firstName, String lastName, String email, String phone, String bio) {
        this.barbershop = barbershop;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.bio = bio;
        this.isActive = true;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Barbershop getBarbershop() { return barbershop; }
    public void setBarbershop(Barbershop barbershop) { this.barbershop = barbershop; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    // Helper method to get full name
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
