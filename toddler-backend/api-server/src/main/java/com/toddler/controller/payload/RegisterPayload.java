package com.toddler.controller.payload;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterPayload(

        @Email(message = "Некорректный формат email.")
        @NotBlank(message = "Email не может быть пустым.")
        String email,

        @NotBlank(message = "Имя не может быть пустым.")
        String username,

        @NotBlank(message = "Имя не может быть пустым.")
        String firstName,

        @NotBlank(message = "Фамилия не может быть пустой.")
        String lastName,

        @Size(min = 6, message = "Пароль должен содержать не менее 6 символов.")
        @NotBlank(message = "Пароль не может быть пустым.")
        String password

) {
    public RegisterPayload {
        if (password.isBlank()) {
            throw new IllegalArgumentException("Пароль не может быть пустым.");
        }
    }
}
