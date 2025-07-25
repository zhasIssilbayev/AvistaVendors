package kz.project.controller;

import kz.project.BotManager;
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
    public ResponseEntity<?> createGetCookies(@RequestBody BotLoginRequest req) {
        Map<String, String> cookies = null;
        try {
            cookies = botManager.loginAndGetCookies(req);
        } catch (LoginFailedException e) {
            return ResponseEntity
                    .status(400)
                    .body(new ErrorResponse("LOGIN_FAILED", e.getMessage()));
        }
        return ResponseEntity.ok(Map.of("data", cookies));
    }
}