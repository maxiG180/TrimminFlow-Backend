package com.trimminflow.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class BaseBarberRequest {

    @Email(message = "Invalid email format")
    private String email;

    @Pattern(regexp = "^$|^[\\+]?[(]?[0-9]{1,3}[)]?[-\\s\\.]?[(]?[0-9]{1,4}[)]?[-\\s\\.]?[0-9]{1,4}[-\\s\\.]?[0-9]{1,9}$",
             message = "Please provide a valid phone number")
    private String phone;

    @Size(max = 500, message = "Bio must be less than 500 characters")
    private String bio;

    @Size(max = 500, message = "Image URL must be less than 500 characters")
    private String profileImageUrl;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
}
