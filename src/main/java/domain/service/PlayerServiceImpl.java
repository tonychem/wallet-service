package domain.service;

import domain.exception.BadCredentialsException;
import domain.exception.DeficientBalanceException;
import domain.exception.PlayerAlreadyExistsException;
import domain.model.Player;
import domain.model.Transaction;
import domain.model.TransferRequestStatus;
import domain.model.dto.*;
import domain.repository.inmemoryimpl.InMemoryPlayerCrudRepositoryImpl;
import domain.repository.inmemoryimpl.InMemoryTransactionCrudeRepositoryImpl;
import domain.repository.PlayerCrudRepository;
import domain.repository.TransactionCrudRepository;
import domain.repository.jdbcimpl.PGJDBCPlayerCrudRepositoryImpl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayerServiceImpl implements PlayerService {
    private final PlayerCrudRepository playerRepository;
    private final TransactionCrudRepository transactionRepository;

    public PlayerServiceImpl() {
//        this.playerRepository = new InMemoryPlayerCrudRepositoryImpl();
        this.playerRepository = new PGJDBCPlayerCrudRepositoryImpl();
        this.transactionRepository = new InMemoryTransactionCrudeRepositoryImpl();
    }

    public PlayerServiceImpl(PlayerCrudRepository playerRepository, TransactionCrudRepository transactionRepository) {
        this.playerRepository = playerRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public AuthenticatedPlayerDto authenticate(String login, byte[] password) throws BadCredentialsException {
        Player player = playerRepository.getByLogin(login);

        if (!Arrays.equals(player.getPassword(), password)) throw new BadCredentialsException("Некорректный пароль");

        return new AuthenticatedPlayerDto(player.getId(), player.getLogin(), player.getUsername(),
                player.getBalance());
    }

    @Override
    public AuthenticatedPlayerDto register(PlayerCreationRequest playerCreationRequest) throws BadCredentialsException {
        try {
            Player newPlayer = Player.builder()
                    .username(playerCreationRequest.getUsername())
                    .login(playerCreationRequest.getLogin())
                    .password(playerCreationRequest.getPassword())
                    .build();

            Player player = playerRepository.create(newPlayer);

            return new AuthenticatedPlayerDto(player.getId(), player.getLogin(), player.getUsername(),
                    player.getBalance());
        } catch (PlayerAlreadyExistsException e) {
            throw new BadCredentialsException(e.getMessage());
        }
    }

    @Override
    public AuthenticatedPlayerDto getBalance(Long id) {
        Player player = playerRepository.getById(id);
        return new AuthenticatedPlayerDto(player.getId(), player.getLogin(), player.getUsername(),
                player.getBalance());
    }

    @Override
    public MoneyTransferResponse transferMoneyTo(MoneyTransferRequest moneyTransferRequest) {
        Transaction transaction = transactionRepository.create(moneyTransferRequest);
        return transferMoneyBetweenAccounts(transaction, moneyTransferRequest);
    }

    @Override
    public MoneyTransferResponse requestMoneyFrom(MoneyTransferRequest moneyTransferRequest) {
        Transaction transaction = transactionRepository.create(moneyTransferRequest);
        Player requester = playerRepository.getByLogin(moneyTransferRequest.getMoneyTo());
        Player donor = playerRepository.getByLogin(moneyTransferRequest.getMoneyFrom());

        return new MoneyTransferResponse(
                new AuthenticatedPlayerDto(requester.getId(), requester.getLogin(),
                        requester.getUsername(), requester.getBalance()),
                new TransactionDto(transaction.getId(), transaction.getStatus(),
                        donor.getUsername(), transaction.getRecipient(), transaction.getAmount())
        );
    }

    @Override
    public Collection<MoneyTransferRequest> getPendingMoneyRequests(String login) {
        Collection<Transaction> transactions = transactionRepository.getTransactionsBySenderAndRecipientAndStatus(
                login, null, TransferRequestStatus.PENDING
        );

        return transactions.stream()
                .map(transaction -> new MoneyTransferRequest(transaction.getId(),
                        transaction.getSender(), transaction.getRecipient(), transaction.getAmount()))
                .collect(Collectors.toList());
    }

    @Override
    public MoneyTransferResponse approvePendingMoneyRequest(String donorUsername, UUID requestId) {
        Transaction transaction = transactionRepository.approveTransaction(donorUsername, requestId);

        return transferMoneyBetweenAccounts(transaction,
                new MoneyTransferRequest(
                        transaction.getId(),
                        transaction.getSender(),
                        transaction.getRecipient(),
                        transaction.getAmount())
        );
    }

    @Override
    public void declinePendingRequest(String donorUsername, UUID requestId) {
        transactionRepository.declineTransaction(donorUsername, requestId);
    }

    @Override
    public Collection<TransactionDto> getHistory(String login, PlayerAction action) {
        Collection<Transaction> transactionsByUser;

        switch (action) {
            case DEBIT -> transactionsByUser = transactionRepository
                    .getTransactionsBySenderAndRecipientAndStatus(login, null, null);
            case CREDIT -> transactionsByUser = transactionRepository
                    .getTransactionsBySenderAndRecipientAndStatus(null, login, null);
            default -> throw new NullPointerException();
        }

        return transactionsByUser.stream()
                .map(transaction -> new TransactionDto(
                        transaction.getId(), transaction.getStatus(), transaction.getSender(),
                        transaction.getRecipient(), transaction.getAmount()
                ))
                .collect(Collectors.toList());
    }

    private MoneyTransferResponse transferMoneyBetweenAccounts(Transaction transaction,
                                                               MoneyTransferRequest moneyTransferRequest) {
        Player sender = playerRepository.getByLogin(moneyTransferRequest.getMoneyFrom());
        Player recipient = playerRepository.getByLogin(moneyTransferRequest.getMoneyTo());

        BigDecimal balanceAfterMoneyWithdrawal = sender.getBalance().subtract(moneyTransferRequest.getAmount());

        if (balanceAfterMoneyWithdrawal.signum() < 0) {
            transaction.setStatus(TransferRequestStatus.FAILED);
            throw new DeficientBalanceException(
                    String.format("Не хватает деньги на балансе игрока с id=%d", sender.getId())
            );
        }

        sender.setBalance(balanceAfterMoneyWithdrawal);
        recipient.setBalance(recipient.getBalance().add(moneyTransferRequest.getAmount()));
        transaction.setStatus(TransferRequestStatus.APPROVED);

        return new MoneyTransferResponse(
                new AuthenticatedPlayerDto(sender.getId(), sender.getLogin(), sender.getUsername(),
                        sender.getBalance()),
                new TransactionDto(transaction.getId(), transaction.getStatus(),
                        transaction.getSender(), transaction.getRecipient(), transaction.getAmount())
        );
    }
}
