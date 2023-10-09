package infrastructure;

import application.*;
import application.exception.UnauthorizedOperationException;
import domain.exception.BadCredentialsException;
import domain.exception.NoSuchPlayerException;
import domain.exception.NoSuchTransactionException;
import domain.exception.TransactionStatusException;
import domain.model.dto.MoneyTransferRequest;
import domain.model.dto.PlayerCreationRequest;
import domain.model.dto.TransactionDto;
import domain.service.PlayerAction;
import lombok.SneakyThrows;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.util.*;

public class ControllerUI {
    private final ApplicationController controller;
    private final MessageDigest messageDigest;
    private Session session;

    @SneakyThrows
    public ControllerUI() {
        this.messageDigest = MessageDigest.getInstance("MD5");
        this.controller = ApplicationControllerFactory.getInstance();
    }

    public void beginInteraction() {
        try (Scanner scan = new Scanner(System.in)) {
            Holder<Boolean> applicationExitRequestHolder = Holder.of(Boolean.FALSE);

            while (!applicationExitRequestHolder.getValue()) {
                authenticationWindowCall(scan, applicationExitRequestHolder);
                if (applicationExitRequestHolder.getValue()) return;
                assert session != null;

                printAuthenticatedUserMenu();

                Holder<Boolean> mainMenuExitRequestHolder = Holder.of(Boolean.FALSE);

                while (!mainMenuExitRequestHolder.getValue()) {
                    authenticatedUserMainMenuCall(scan, mainMenuExitRequestHolder);
                }
            }
        }
    }

    private void printLandingMenu() {
        String landingMenu = """
                Wallet-service-app
                1. Зарегистрировать нового пользователя
                2. Авторизоваться
                Любая клавиша. ВЫХОД.""";
        System.out.println(landingMenu);
    }

    private int readInputKey(Scanner scan) {
        int value = -1;

        String input = scan.nextLine();

        while (value == -1) {
            try {
                value = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Не цифра! Попробуйте еще");
            }
        }
        return value;
    }

    private void authenticationWindowCall(Scanner scan, Holder<Boolean> exitRequest) {
        printLandingMenu();

        int authOrRegisterOrExit = readInputKey(scan);

        switch (authOrRegisterOrExit) {
            case 1 -> {
                AuthenticationDto authenticationDto = registerUser(scan);

                this.session = new Session(authenticationDto.getId(), authenticationDto.getLogin(),
                        authenticationDto.getUsername(),
                        authenticationDto.getSessionId());

                System.out.println("Успешно зарегистрирован пользователь с ником: "
                        + authenticationDto.getUsername());
            }
            case 2 -> {
                AuthenticationDto authenticationDto = authenticateUser(scan);

                this.session = new Session(authenticationDto.getId(), authenticationDto.getLogin(),
                        authenticationDto.getUsername(),
                        authenticationDto.getSessionId());

                System.out.println("Успешно авторизован пользователь: "
                        + authenticationDto.getUsername());
            }
            case 3 -> exitRequest = new Holder<>(Boolean.TRUE);
        }
    }

    private void authenticatedUserMainMenuCall(Scanner scan, Holder<Boolean> mainMenuExitHolder) {
        int authenticatedUserOptions = readInputKey(scan);

        switch (authenticatedUserOptions) {
            case 1 -> {
                try {
                    BalanceDto balance =
                            controller.getBalance(session.getUserId(), session.getSessionId());
                    System.out.printf("Ваш текущий баланс: %.2f%n", balance.getBalance().doubleValue());
                } catch (UnauthorizedOperationException e) {
                    System.err.println("Произошла ошибка, попробуйте еще раз");
                }
            }

            case 2 -> {
                try {
                    System.out.println("Введите логин, кому хотите отправить деньги: ");
                    String recipient = scan.nextLine();

                    double amount = Double.MIN_VALUE;

                    System.out.print("Введите сумму (положительное десятичное число): ");
                    String input = scan.nextLine();

                    while (amount == Double.MIN_VALUE) {
                        try {
                            amount = Double.parseDouble(input);
                            if (amount < 0) throw new InputMismatchException();
                        } catch (InputMismatchException e) {
                            System.err.println("Это отрицательное десятичное число!");
                        } catch (NumberFormatException e) {
                            System.err.println("Это не десятичное число!");
                        }
                    }

                    controller.transferMoney(session.getLogin(), recipient, BigDecimal.valueOf(amount),
                            session.getSessionId(), UUID.randomUUID());
                } catch (NoSuchPlayerException nsp) {
                    System.err.println(nsp.getMessage());
                } catch (UnauthorizedOperationException e) {
                    System.err.println("Ошибка авторизации! Попробуйте авторизоваться еще раз");
                }
            }

            case 3 -> {
                try {
                    System.out.println("Введите логин, от которого хотите получить деньги: ");
                    String donor = scan.nextLine();

                    double amount = Double.MIN_VALUE;

                    System.out.print("Введите сумму (положительное десятичное число): ");
                    String input = scan.nextLine();

                    while (amount == Double.MIN_VALUE) {
                        try {
                            amount = Double.parseDouble(input);
                            if (amount < 0) throw new InputMismatchException();
                        } catch (InputMismatchException e) {
                            System.err.println("Это отрицательное десятичное число!");
                        } catch (NumberFormatException e) {
                            System.err.println("Это не десятичное число!");
                        }
                    }

                    controller.requestMoneyFrom(session.getUsername(), donor, BigDecimal.valueOf(amount),
                            session.getSessionId(), UUID.randomUUID());
                } catch (NoSuchPlayerException nsp) {
                    System.err.println(nsp.getMessage());
                } catch (UnauthorizedOperationException e) {
                    System.err.println("Ошибка авторизации! Попробуйте авторизоваться еще раз");
                }
            }

            case 4 -> {
                try {
                    Collection<MoneyTransferRequest> requests =
                            controller.getPendingMoneyRequests(session.getLogin(), session.getSessionId());

                    if (requests.isEmpty()) {
                        System.out.println("У вас нет неодобренных заявок на списание");
                        return;
                    }

                    requests.stream()
                            .forEach(request -> System.out.printf("id операции = %s, запрос от игрока %s, сумма %.2f%n",
                                    request.getId(), request.getMoneyTo(), request.getAmount().doubleValue()));
                } catch (UnauthorizedOperationException e) {
                    System.err.println("Ошибка авторизации! Попробуйте авторизоваться еще раз");
                }
            }

            case 5 -> {
                System.out.println("Введите id транзаций для подтверждения через запятую: ");
                String[] transactionStringView = scan.nextLine().split(",");
                List<UUID> ids = new ArrayList<>();

                for (String transaction : transactionStringView) {
                    try {
                        UUID id = UUID.fromString(transaction);
                        ids.add(id);
                    } catch (Exception e) {
                        System.err.println("Ошибка парсинга id транзакции");
                    }
                }

                for (UUID id : ids) {
                    try {
                        controller.approvePendingRequest(session.getSessionId(), id);
                    } catch (NoSuchTransactionException | TransactionStatusException e) {
                        System.err.println(e.getMessage());
                    } catch (UnauthorizedOperationException e) {
                        System.err.println("Ошибка авторизации! Попробуйте авторизоваться еще раз");
                    }
                }
            }

            case 6 -> {
                System.out.println("Введите id транзаций для отмены через запятую: ");
                String[] transactionStringView = scan.nextLine().split(",");
                List<UUID> ids = new ArrayList<>();

                for (String transaction : transactionStringView) {
                    try {
                        UUID id = UUID.fromString(transaction);
                        ids.add(id);
                    } catch (Exception e) {
                        System.err.println("Ошибка парсинга id транзакции");
                    }
                }

                for (UUID id : ids) {
                    try {
                        controller.declinePendingRequest(session.getSessionId(), id);
                    } catch (NoSuchTransactionException | TransactionStatusException e) {
                        System.err.println(e.getMessage());
                    } catch (UnauthorizedOperationException e) {
                        System.err.println("Ошибка авторизации! Попробуйте авторизоваться еще раз");
                    }
                }
            }

            case 7 -> {
                try {
                    String options = """
                            Введите:
                            debit - получить историю по списаниям
                            credit - получить историю по зачислениям
                            ЛЮБОЙ СИМВОЛ - по всем операциям""";

                    System.out.println(options);

                    String userChoice = scan.nextLine().toLowerCase();

                    Collection<TransactionDto> history;

                    switch (userChoice) {
                        case "debit" -> history = controller.getHistory(session.getLogin(),
                                PlayerAction.DEBIT, session.getSessionId());
                        case "credit" -> history = controller.getHistory(session.getLogin(),
                                PlayerAction.CREDIT, session.getSessionId());
                        default -> {
                            Collection<TransactionDto> creditHistory = controller.getHistory(session.getLogin(),
                                    PlayerAction.CREDIT, session.getSessionId());
                            Collection<TransactionDto> debitHistory = controller.getHistory(session.getLogin(),
                                    PlayerAction.DEBIT, session.getSessionId());
                            creditHistory.addAll(debitHistory);
                            history = creditHistory;
                        }
                    }

                    if (history.isEmpty()) {
                        System.out.println("Для данного аккаунта не найдено никаких операций");
                        return;
                    }

                    history.stream()
                            .forEach(transaction ->
                                    System.out.printf("id = %s, отправитель %s, получатель %s, сумма %.2f%n",
                                            transaction.getId(), transaction.getSender(), transaction.getRecipient(),
                                            transaction.getAmount().doubleValue()));

                } catch (NoSuchPlayerException nsp) {
                    System.err.println(nsp.getMessage());
                } catch (UnauthorizedOperationException e) {
                    System.err.println("Ошибка авторизации! Попробуйте авторизоваться еще раз");
                }
            }

            case 8 -> {
                controller.signOut(session.getSessionId());
                this.session = null;
                mainMenuExitHolder.setValue(Boolean.TRUE);
            }
        }
    }

    private PlayerCreationRequest gatherRegisterInfo(Scanner scanner) {
        String login;
        String password;
        String username;

        System.out.print("Введите логин: ");
        do {
            login = scanner.nextLine();
        } while (login == null || login.isEmpty());

        System.out.print("Введите пароль: ");
        do {
            password = scanner.nextLine();
        } while (password == null || password.isEmpty());

        System.out.print("Введите имя пользователя: ");
        do {
            username = scanner.nextLine();
        } while (username == null || username.isEmpty());

        byte[] passEncoded = messageDigest.digest(password.getBytes());
        return new PlayerCreationRequest(login, passEncoded, username);
    }

    private AuthenticationDto registerUser(Scanner scanner) {
        AuthenticationDto authenticationDto = null;

        while (authenticationDto == null) {
            try {
                PlayerCreationRequest request = gatherRegisterInfo(scanner);
                authenticationDto = controller.registerUser(request);
            } catch (BadCredentialsException e) {
                System.err.println("Ошибка в данных: " + e.getMessage() + "\n" + "Попробуйте еще раз");
            }
        }

        return authenticationDto;
    }

    private AuthenticationDto authenticateUser(Scanner scanner) {
        AuthenticationDto authenticationDto = null;

        while (authenticationDto == null) {
            try {
                AuthenticationRequest request = gatherAuthenticationInfo(scanner);
                authenticationDto = controller.authenticate(request);
            } catch (NoSuchPlayerException e) {
                System.err.println("Не найден пользователь с указанным логином");
            } catch (BadCredentialsException e) {
                System.err.println("Ошибка в данных: " + e.getMessage() + "\n" + "Попробуйте еще раз");
            }
        }

        return authenticationDto;
    }

    private AuthenticationRequest gatherAuthenticationInfo(Scanner scanner) {
        String login;
        String password;

        System.out.print("Введите логин: ");
        do {
            login = scanner.nextLine();
        } while (login == null || login.isEmpty());

        System.out.print("Введите пароль: ");

        do {
            password = scanner.nextLine();
        } while (password == null || password.isEmpty());

        byte[] passEncoded = messageDigest.digest(password.getBytes());
        return new AuthenticationRequest(login, passEncoded);
    }

    private void printAuthenticatedUserMenu() {
        String userMainMenu = """
                1. Узнать ваш текущий баланс
                2. Перевести деньги на счет другого игрока
                3. Оставить заявку пользователю для получения денежных средств
                4. Посмотреть запросы на списание денежных средств другим пользователям
                5. Подтвердить запросы на списание денежных средств от других пользователей
                6. Отклонить запросы на списание денежных средств от других пользователей
                7. Посмотреть Вашу историю на аккаунте
                8. Выйти из аккаунта""";
        System.out.println(userMainMenu);
    }
}
