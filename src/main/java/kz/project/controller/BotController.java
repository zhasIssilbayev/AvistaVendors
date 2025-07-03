package kz.project.controller;

import kz.project.BotManager;
import kz.project.dto.BotLoginRequest;
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

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody BotLoginRequest req) {
        Map<String, String> cookies = botManager.loginAndGetCookies(req);
        return ResponseEntity.ok(Map.of("data", cookies));
    }
}