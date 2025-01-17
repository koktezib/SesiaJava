package model;

import java.time.LocalDateTime;
import java.time.Duration;

public class ShortLink {
    private String linkId;             // Уникальный идентификатор сокращённой ссылки, например "3DZHeG"
    private String originalUrl;        // Длинный URL
    private String shortUrl;           // Полный короткий URL, например "http://clck.ru/3DZHeG"
    private String userUuid;           // UUID пользователя
    private LocalDateTime createdAt;   // Время создания ссылки
    private Duration ttl;              // Срок жизни (например, 24 часа)
    private int maxRedirects;          // Лимит переходов
    private int currentRedirects;      // Текущее количество переходов

    public ShortLink(String linkId, String originalUrl, String shortUrl, String userUuid,
                     LocalDateTime createdAt, Duration ttl, int maxRedirects) {
        this.linkId = linkId;
        this.originalUrl = originalUrl;
        this.shortUrl = shortUrl;
        this.userUuid = userUuid;
        this.createdAt = createdAt;
        this.ttl = ttl;
        this.maxRedirects = maxRedirects;
        this.currentRedirects = 0;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public String getUserUuid() {
        return userUuid;
    }

    public void incrementRedirects() {
        this.currentRedirects++;
    }

    public boolean isExpired() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(createdAt.plus(ttl));
    }

    public boolean isRedirectLimitReached() {
        return currentRedirects >= maxRedirects;
    }
}
