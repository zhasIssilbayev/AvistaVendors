package kz.project.controller;

import kz.project.BotManager;
import kz.project.dto.ApiResponse;
import kz.project.dto.BotLoginRequest;
import kz.project.dto.ErrorResponse;
import kz.project.exception.LoginFailedException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class BotController {
    private final BotManager botManager;

    public BotController(BotManager botManager) {
        this.botManager = botManager;
    }

    @PostMapping("/createGetCookies")
    public ResponseEntity<?> createGetCookies(@RequestBody BotLoginRequest req) throws LoginFailedException {
        Map<String, String> cookies = botManager.loginAndGetCookies(req);
        return ResponseEntity.ok(new ApiResponse<>(cookies));
    }
}