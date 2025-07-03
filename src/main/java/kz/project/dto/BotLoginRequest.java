package kz.project.dto;

import kz.project.bots.BotTypes;

public record BotLoginRequest(BotTypes botType, String login, String password, String url) {}