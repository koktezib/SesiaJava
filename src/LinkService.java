package service;

import model.ShortLink;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LinkService {

    // Хранилище ссылок: key = linkId, value = ShortLink
    private Map<String, ShortLink> storage = new HashMap<>();

    // Сокращённая "база" для URL
    private static final String BASE_SHORT_URL = "http://clck.ru/";

    /**
     * Создаёт новую короткую ссылку.
     * @param originalUrl исходный длинный URL
     * @param userUuid    UUID пользователя
     * @param ttl         срок жизни (Duration)
     * @param maxRedirects лимит переходов
     * @return объект ShortLink
     */
    public ShortLink createShortLink(String originalUrl, String userUuid, Duration ttl, int maxRedirects) {
        // Генерируем linkId (для простоты — часть UUID)
        String linkId = generateLinkId();
        String shortUrl = BASE_SHORT_URL + linkId;

        ShortLink shortLink = new ShortLink(
                linkId,
                originalUrl,
                shortUrl,
                userUuid,
                LocalDateTime.now(),
                ttl,
                maxRedirects
        );

        // Сохраняем в памяти
        storage.put(linkId, shortLink);
        return shortLink;
    }

    /**
     * Ищет ссылку по linkId и возвращает её, если она существует.
     * @param linkId идентификатор сокращённой ссылки
     * @return ShortLink или null
     */
    public ShortLink getShortLink(String linkId) {
        return storage.get(linkId);
    }

    /**
     * Проверяет доступность ссылки (не просрочена ли и не превышен ли лимит).
     * Увеличивает счётчик переходов, если всё ок.
     *
     * @param linkId идентификатор сокращённой ссылки
     * @return originalUrl или null, если ссылка недоступна
     */
    public String redirect(String linkId) {
        ShortLink shortLink = storage.get(linkId);
        if (shortLink == null) {
            return null; // Ссылки нет
        }

        // Проверяем срок жизни
        if (shortLink.isExpired()) {
            // Ссылка просрочена, сообщаем и удаляем
            storage.remove(linkId);
            System.out.println("Ссылка просрочена и была удалена.");
            return null;
        }

        // Проверяем лимит переходов
        if (shortLink.isRedirectLimitReached()) {
            System.out.println("Лимит переходов исчерпан! Ссылка недоступна.");
            return null;
        }

        // Всё в порядке — увеличиваем счётчик
        shortLink.incrementRedirects();
        return shortLink.getOriginalUrl();
    }

    /**
     * Удаление ссылки (только владелец имеет право).
     *
     * @param linkId   идентификатор сокращённой ссылки
     * @param userUuid UUID пользователя
     * @return true, если ссылка была удалена
     */
    public boolean deleteShortLink(String linkId, String userUuid) {
        ShortLink shortLink = storage.get(linkId);
        if (shortLink != null && shortLink.getUserUuid().equals(userUuid)) {
            storage.remove(linkId);
            return true;
        }
        return false;
    }

    /**
     * Периодическая проверка и удаление "протухших" ссылок.
     * Можно вызывать по расписанию.
     */
    public void cleanup() {
        storage.values().removeIf(ShortLink::isExpired);
    }

    /**
     * Генерация уникального linkId.
     */
    private String generateLinkId() {
        // Упрощённая генерация. Можно улучшить, чтобы ссылка была короче.
        return UUID.randomUUID().toString().substring(0, 6);
    }

    public static  boolean isValidUrl(String urlStr) {
        try {
            new URL(urlStr);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }
}
