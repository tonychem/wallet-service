package domain.service;

import domain.exception.BadCredentialsException;
import domain.exception.DeficientBalanceException;
import domain.exception.UserAlreadyExistsException;
import domain.model.Player;
import domain.model.Transaction;
import domain.model.TransferRequestStatus;
import domain.model.dto.*;
import domain.repository.InMemoryPlayerCrudRepositoryImpl;
import domain.repository.InMemoryTransactionCrudeRepositoryImpl;
import domain.repository.PlayerCrudRepository;
import domain.repository.TransactionCrudRepository;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayerServiceImpl implements PlayerService {
    private final PlayerCrudRepository playerRepository;
    private final TransactionCrudRepository transactionRepository;

    public PlayerServiceImpl() {
        this.playerRepository = new InMemoryPlayerCrudRepositoryImpl();
        this.transactionRepository = new InMemoryTransactionCrudeRepositoryImpl();
    }

    @Override
    public AuthenticatedPlayerDto authenticate(String login, byte[] password) throws BadCredentialsException {
        Player player = playerRepository.getByLogin(login);

        if (!Arrays.equals(player.getPassword(), password)) throw new BadCredentialsException("Incorrect password");

        return new AuthenticatedPlayerDto(player.getId(), player.getUsername(), player.getBalance());
    }

    @Override
    public AuthenticatedPlayerDto register(PlayerCreationRequest playerCreationRequest) throws BadCredentialsException {
        try {
            Player player = playerRepository.create(playerCreationRequest);
            return new AuthenticatedPlayerDto(player.getId(), player.getUsername(), player.getBalance());
        } catch (UserAlreadyExistsException e) {
            throw new BadCredentialsException(e.getMessage());
        }
    }

    @Override
    public BigDecimal getBalance(String login) {
        return playerRepository.getByLogin(login).getBalance();
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

        return new MoneyTransferResponse(
                new AuthenticatedPlayerDto(requester.getId(), requester.getUsername(), requester.getBalance()),
                new TransactionDto(transaction.getId(), transaction.getStatus(),
                        transaction.getSender(), transaction.getRecipient(), transaction.getAmount())
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
    public MoneyTransferResponse approvePendingMoneyRequest(UUID requestId) {
        Transaction transaction = transactionRepository.getById(requestId);

        return transferMoneyBetweenAccounts(transaction,
                new MoneyTransferRequest(
                        transaction.getId(),
                        transaction.getSender(),
                        transaction.getRecipient(),
                        transaction.getAmount())
        );
    }

    @Override
    public void declinePendingRequest(UUID requestId) {
        Transaction transaction = transactionRepository.getById(requestId);
        transaction.setStatus(TransferRequestStatus.DECLINED);
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
                    String.format("Not enough money on user's balance id=%d", sender.getId())
            );
        }

        sender.setBalance(balanceAfterMoneyWithdrawal);
        recipient.setBalance(recipient.getBalance().add(moneyTransferRequest.getAmount()));
        transaction.setStatus(TransferRequestStatus.APPROVED);

        return new MoneyTransferResponse(
                new AuthenticatedPlayerDto(sender.getId(), sender.getUsername(), sender.getBalance()),
                new TransactionDto(transaction.getId(), transaction.getStatus(),
                        transaction.getSender(), transaction.getRecipient(), transaction.getAmount())
        );
    }
}
