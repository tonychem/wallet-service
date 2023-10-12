package infrastructure;

import application.ApplicationController;
import application.ApplicationControllerFactory;
import application.dto.AuthenticationDto;
import application.dto.AuthenticationRequest;
import application.dto.BalanceDto;
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

/**
 * Класс, ответственный за консольное взаимодействие с пользователем.
 */
public class ControllerUI {
    private final ApplicationController controller;
    private final MessageDigest messageDigest;
    private Session session;

    private static final String LANDING_MENU = """
            Wallet-service-app
            1. Зарегистрировать нового пользователя
            2. Авторизоваться
            3. ВЫХОД.""";

    private static final String PLAYER_MAIN_MENU = """
            1. Узнать ваш текущий баланс
            2. Перевести деньги на счет другого игрока
            3. Оставить заявку пользователю для получения денежных средств
            4. Посмотреть запросы на списание денежных средств другим пользователям
            5. Подтвердить запросы на списание денежных средств от других пользователей
            6. Отклонить запросы на списание денежных средств от других пользователей
            7. Посмотреть Вашу историю на аккаунте
            8. Выйти из аккаунта""";

    @SneakyThrows
    public ControllerUI() {
        this.messageDigest = MessageDigest.getInstance("MD5");
        this.controller = ApplicationControllerFactory.getInstance();
    }

    /**
     * Главный метод, отвественный за пользовательский поток управления
     */
    public void beginInteraction() {
        try (Scanner scan = new Scanner(System.in)) {
            Holder<Boolean> fullExitSignalHolder = Holder.of(Boolean.FALSE);

            while (!fullExitSignalHolder.getValue()) {
                authenticationWindowCall(scan, fullExitSignalHolder);

                if (fullExitSignalHolder.getValue()) {
                    return;
                }

                assert session != null;

                printAuthenticatedUserMenu();

                Holder<Boolean> mainMenuExitSignalHolder = Holder.of(Boolean.FALSE);

                while (!mainMenuExitSignalHolder.getValue()) {
                    authenticatedUserMainMenuCall(scan, mainMenuExitSignalHolder);
                }
            }
        }
    }

    /**
     * Метод вызова меню аутентификации
     *
     * @param scan
     * @param exitRequest
     */
    private void authenticationWindowCall(Scanner scan, Holder<Boolean> exitRequest) {
        printLandingMenu();

        int authOrRegisterOrExit = readInputKey(scan);

        switch (authOrRegisterOrExit) {
            case 1 -> {
                AuthenticationDto authenticationDto = registerUser(scan);
                createNewSession(authenticationDto);
                System.out.println("Успешно зарегистрирован пользователь с ником: "
                        + authenticationDto.getUsername());
            }
            case 2 -> {
                AuthenticationDto authenticationDto = authenticateUser(scan);
                createNewSession(authenticationDto);
                System.out.println("Успешно авторизован пользователь: "
                        + authenticationDto.getUsername());
            }
            case 3 -> exitRequest = new Holder<>(Boolean.TRUE);
        }
    }

    /**
     * Метод вызова основного меню взаимодействия с авторизованным пользователем
     *
     * @param scan
     * @param mainMenuExitHolder
     */
    private void authenticatedUserMainMenuCall(Scanner scan, Holder<Boolean> mainMenuExitHolder) {
        int authenticatedUserOptions = readInputKey(scan);

        switch (authenticatedUserOptions) {
            case 1 -> balancePrinter();
            case 2 -> transferMoneyHandler(scan);
            case 3 -> requestMoneyHandler(scan);
            case 4 -> pendingMoneyRequestsPrinter();
            case 5 -> moneyRequestApprovalHandler(scan);
            case 6 -> declinePendingRequestsHandler(scan);
            case 7 -> historyPrinter(scan);
            case 8 -> signOutHandler(mainMenuExitHolder);
        }
    }

    private void printLandingMenu() {
        System.out.println(LANDING_MENU);
    }

    private int readInputKey(Scanner scan) {
        int value = -1;

        String input = scan.nextLine();

        while (value == -1) {
            try {
                value = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Не цифра! Попробуйте еще");
                scan.nextLine();
            }
        }
        return value;
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

    private void createNewSession(AuthenticationDto authenticationDto) {
        this.session = new Session(authenticationDto.getId(), authenticationDto.getLogin(),
                authenticationDto.getUsername(),
                authenticationDto.getSessionId());
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
        System.out.println(PLAYER_MAIN_MENU);
    }

    private void balancePrinter() {
        try {
            BalanceDto balance =
                    controller.getBalance(session.getUserId(), session.getSessionId());
            System.out.printf("Ваш текущий баланс: %.2f%n", balance.getBalance().doubleValue());
        } catch (UnauthorizedOperationException e) {
            System.err.println("Произошла ошибка, попробуйте еще раз");
        }
    }

    private void transferMoneyHandler(Scanner scan) {
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

    private void requestMoneyHandler(Scanner scan) {
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

    private void pendingMoneyRequestsPrinter() {
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

    private void moneyRequestApprovalHandler(Scanner scan) {
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
                controller.approvePendingRequest(session.getSessionId(), session.getUsername(), id);
            } catch (NoSuchTransactionException | TransactionStatusException e) {
                System.err.println(e.getMessage());
            } catch (UnauthorizedOperationException e) {
                System.err.println("Ошибка авторизации! Попробуйте авторизоваться еще раз");
            }
        }
    }

    private void declinePendingRequestsHandler(Scanner scan) {
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
                controller.declinePendingRequest(session.getSessionId(), session.getUsername(), id);
            } catch (NoSuchTransactionException | TransactionStatusException e) {
                System.err.println(e.getMessage());
            } catch (UnauthorizedOperationException e) {
                System.err.println("Ошибка авторизации! Попробуйте авторизоваться еще раз");
            }
        }
    }

    private void historyPrinter(Scanner scan) {
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

    private void signOutHandler(Holder<Boolean> mainMenuExitHolder) {
        controller.signOut(session.getLogin(), session.getSessionId());
        this.session = null;
        mainMenuExitHolder.setValue(Boolean.TRUE);
    }
}
