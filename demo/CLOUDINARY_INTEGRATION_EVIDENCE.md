# Cloudinary Integration Evidence

This document outlines the integration of Cloudinary for handling image uploads in the TrimminFlow application. This solution replaces local storage or direct database storage for images, providing a scalable, secure, and optimized way to manage media assets (specifically Barber profile images).

## 1. Backend Implementation (Spring Boot)

### A. Dependencies
We added the Cloudinary Java library to `build.gradle` to interact with the Cloudinary API.
```gradle
implementation 'com.cloudinary:cloudinary-http44:1.36.0'
```

### B. Configuration (`CloudinaryConfig.java`)
A configuration class was created to initialize the `Cloudinary` bean with credentials from `application.properties`. This ensures secure access to the Cloudinary cloud environment.

```java
@Configuration
public class CloudinaryConfig {
    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
            "cloud_name", cloudName,
            "api_key", apiKey,
            "api_secret", apiSecret,
            "secure", true
        ));
    }
}
```

### C. Service Layer (`CloudinaryService.java`)
A dedicated service handles the actual upload logic. It accepts a `MultipartFile`, uploads it to Cloudinary, and returns the secure URL.
*   **Key Method**: `uploadImage(MultipartFile file)`
*   **Functionality**: Converts the file to bytes and uploads it using `cloudinary.uploader().upload()`.

### D. Controller Layer (`BarberController.java`)
The `BarberController` was updated to handle `multipart/form-data` requests.
*   **Endpoint**: `POST /api/v1/barbers` and `PUT /api/v1/barbers/{id}`
*   **Logic**:
    1.  Accepts `@ModelAttribute CreateBarberRequest` and `MultipartFile image`.
    2.  Checks if an image file is provided.
    3.  If yes, calls `cloudinaryService.uploadImage(image)` to get the URL.
    4.  Sets the `profileImageUrl` in the request object before saving to the database.

## 2. Frontend Implementation (Next.js / React)

### A. API Client (`api.ts`)
The API client was refactored to support file uploads.
*   **Usage**: `FormData` object is used instead of JSON for requests that include images.
*   **Headers**: `Content-Type: multipart/form-data` is set automatically by the browser when `FormData` is used.

### B. User Interface (`BarberForm.tsx`)
The form was updated to allow file selection.
*   **Input**: Standard `<input type="file" />` or a custom upload component.
*   **State**: React state manages the selected `File` object.
*   **Submission**: On submit, the file is passed to the `barberApi.create` or `update` methods along with other form fields.

## 3. Workflow Summary
1.  **User** selects an image in the frontend `BarberForm`.
2.  **Frontend** wraps data in `FormData` and sends a `POST` request to the backend.
3.  **Backend Controller** receives the `MultipartFile`.
4.  **CloudinaryService** uploads the file to Cloudinary servers.
5.  **Cloudinary** returns a public URL (e.g., `https://res.cloudinary.com/...`).
6.  **Backend** saves this URL in the `Barber` entity in the PostgreSQL database.
7.  **Result**: The image is hosted on Cloudinary's CDN, ensuring fast loading and reduced load on the application server.
