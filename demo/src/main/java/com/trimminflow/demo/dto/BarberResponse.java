package com.trimminflow.demo.dto;

import com.trimminflow.demo.entity.Barber;

public class BarberResponse extends BaseResponse {

    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String bio;
    private String profileImageUrl;

    public BarberResponse() {
    }

    public BarberResponse(Barber barber) {
        this.setId(barber.getId());
        this.setBarbershopId(barber.getBarbershop().getId());
        this.firstName = barber.getFirstName();
        this.lastName = barber.getLastName();
        this.email = barber.getEmail();
        this.phone = barber.getPhone();
        this.bio = barber.getBio();
        this.profileImageUrl = barber.getProfileImageUrl();
        this.setIsActive(barber.getIsActive());
        this.setCreatedAt(barber.getCreatedAt());
        this.setUpdatedAt(barber.getUpdatedAt());
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
