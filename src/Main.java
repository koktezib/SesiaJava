import service.LinkService;
import model.ShortLink;

import java.awt.Desktop;
import java.net.URI;
import java.time.Duration;
import java.util.Scanner;
import java.util.UUID;

public class Main {

    private static LinkService linkService = new LinkService();

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        Duration linkLifeTime = Duration.ofSeconds(15); //секунды
        String userUuid = generateOrLoadUserUuid();

        System.out.println("Ваш UUID: " + userUuid);

        while (true) {
            System.out.println("\nДоступные команды:");
            System.out.println("1. Создать короткую ссылку");
            System.out.println("2. Перейти по короткой ссылке");
            System.out.println("3. Удалить короткую ссылку");
            System.out.println("4. Выйти");

            System.out.print("Введите номер команды: ");
            String command = scanner.nextLine();

            switch (command) {
                case "1":
                    System.out.print("Введите длинный URL: ");
                    String originalUrl = scanner.nextLine();

                    // Проверка
                    if (!LinkService.isValidUrl(originalUrl)) {
                        System.out.println("Некорректная ссылка! Проверьте формат и попробуйте снова.");
                        break; // Вернёмся в меню
                    }

                    System.out.print("Введите лимит переходов (число): ");
                    int maxRedirects = Integer.parseInt(scanner.nextLine());

                    ShortLink newLink = linkService.createShortLink(originalUrl, userUuid, linkLifeTime, maxRedirects);
                    System.out.println("Короткая ссылка создана: " + newLink.getShortUrl());
                    break;

                case "2":
                    System.out.print("Введите linkId (часть после http://clck.ru/): ");
                    String linkId = scanner.nextLine();

                    // Пытаемся перейти
                    String redirectUrl = linkService.redirect(linkId);
                    if (redirectUrl != null) {
                        System.out.println("Перенаправляем на: " + redirectUrl);
                        // Пробуем открыть браузер
                        try {
                            Desktop.getDesktop().browse(new URI(redirectUrl));
                        } catch (Exception e) {
                            System.out.println("Не удалось открыть браузер автоматически: " + e.getMessage());
                        }
                    } else {
                        System.out.println("Ссылка недоступна!");
                    }
                    break;

                case "3":
                    System.out.print("Введите linkId для удаления: ");
                    String linkIdDel = scanner.nextLine();

                    boolean deleted = linkService.deleteShortLink(linkIdDel, userUuid);
                    if (deleted) {
                        System.out.println("Ссылка удалена.");
                    } else {
                        System.out.println("Ссылка не найдена или вы не являетесь её владельцем.");
                    }
                    break;

                case "4":
                    System.out.println("Выход из программы...");
                    scanner.close();
                    return;

                default:
                    System.out.println("Неизвестная команда!");
                    break;
            }

            // Вызов метода очистки "протухших" ссылок
            linkService.cleanup();
        }
    }

    private static String generateOrLoadUserUuid() {
        // В реальном случае можно загрузить из файла или cookie.
        // Для примера генерирую всегда новый:
        return UUID.randomUUID().toString();
    }
}
