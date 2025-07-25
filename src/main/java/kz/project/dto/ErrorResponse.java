package kz.project.dto;

/**
 * @param error   Код ошибки, например: "LOGIN_FAILED"
 * @param message Человеко-понятное описание
 */
public record ErrorResponse(String error, String message) {
}
