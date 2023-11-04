package ru.yandex.wallet.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.wallet.domain.Player;
import ru.yandex.wallet.domain.Transaction;
import ru.yandex.wallet.domain.TransferRequestStatus;
import ru.yandex.wallet.domain.dto.*;
import ru.yandex.wallet.domain.mapper.MoneyTransferMapper;
import ru.yandex.wallet.domain.mapper.PlayerMapper;
import ru.yandex.wallet.domain.mapper.TransactionMapper;
import ru.yandex.wallet.exception.model.BadCredentialsException;
import ru.yandex.wallet.exception.model.DeficientBalanceException;
import ru.yandex.wallet.exception.model.PlayerAlreadyExistsException;
import ru.yandex.wallet.in.dto.*;
import ru.yandex.wallet.repository.PlayerCrudRepository;
import ru.yandex.wallet.repository.TransactionCrudRepository;
import ru.yandex.wallet.service.PlayerAction;
import ru.yandex.wallet.service.PlayerService;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlayerServiceImpl implements PlayerService {
    private final PlayerCrudRepository playerRepository;
    private final TransactionCrudRepository transactionRepository;
    private final MessageDigest messageDigest;

    private PlayerMapper playerMapper = PlayerMapper.INSTANCE;
    private TransactionMapper transactionMapper = TransactionMapper.INSTANCE;
    private MoneyTransferMapper moneyTransferMapper = MoneyTransferMapper.INSTANCE;

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthenticatedPlayerDto authenticate(UnsecuredAuthenticationRequestDto unsecuredAuthenticationRequest)
            throws BadCredentialsException {
        Player player = playerRepository.getByLogin(unsecuredAuthenticationRequest.getLogin());
        byte[] hashedPassword = messageDigest.digest(unsecuredAuthenticationRequest.getPassword().getBytes());

        if (!Arrays.equals(player.getPassword(), hashedPassword)) {
            throw new BadCredentialsException("Некорректный пароль");
        }

        return playerMapper.toAuthenticatedPlayerDto(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthenticatedPlayerDto register(UnsecuredPlayerCreationRequestDto playerCreationRequest) throws BadCredentialsException {
        try {
            Player newPlayer = Player.builder()
                    .username(playerCreationRequest.getUsername())
                    .login(playerCreationRequest.getLogin())
                    .password(messageDigest.digest(playerCreationRequest.getPassword().getBytes()))
                    .build();

            Player player = playerRepository.create(newPlayer);

            return playerMapper.toAuthenticatedPlayerDto(player);
        } catch (PlayerAlreadyExistsException e) {
            throw new BadCredentialsException(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BalanceDto getBalance(Long id) {
        Player player = playerRepository.getById(id);
        return playerMapper.toBalanceDto(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BalanceDto transferMoneyTo(String sender, PlayerTransferMoneyRequestDto moneyRequest) {
        Player receiver = playerRepository.getByLogin(moneyRequest.getRecipient());
        UUID transactionId = UUID.randomUUID();

        MoneyTransferRequest moneyTransferRequest =
                moneyTransferMapper.toMoneyTransferRequest(transactionId, sender, moneyRequest);
        Transaction transaction = transactionRepository.create(moneyTransferRequest);

        MoneyTransferResponse moneyTransferResponse =
                transferMoneyBetweenAccounts(transaction, moneyTransferRequest);

        return moneyTransferMapper.toBalanceDto(moneyTransferResponse);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MoneyTransferResponse requestMoneyFrom(String requesterLogin, PlayerRequestMoneyDto requestMoneyDto) {
        Player requester = playerRepository.getByLogin(requesterLogin);
        Player donor = playerRepository.getByLogin(requestMoneyDto.getDonor());
        UUID transactionId = UUID.randomUUID();

        MoneyTransferRequest moneyTransferRequest =
                moneyTransferMapper.toMoneyTransferRequest(transactionId, requesterLogin, requestMoneyDto);
        Transaction transaction = transactionRepository.create(moneyTransferRequest);

        return new MoneyTransferResponse(
                playerMapper.toAuthenticatedPlayerDto(requester),
                transactionMapper.toTransactionDto(transaction)
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<MoneyTransferRequest> getPendingMoneyRequests(String login) {
        Collection<Transaction> transactions = transactionRepository.getTransactionsBySenderAndRecipientAndStatus(
                login, null, TransferRequestStatus.PENDING
        );

        return transactions.stream()
                .map(transactionMapper::toMoneyTransferRequest)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<MoneyTransferResponse> approvePendingMoneyRequest(String donorUsername, TransactionsListDto transactionsList) {
        List<UUID> validIds = extractValidUUIDs(transactionsList.getIds());
        List<MoneyTransferResponse> moneyTransferResponseList = new ArrayList<>();

        for (UUID transactionId : validIds) {
            Transaction transaction = transactionRepository.approveTransaction(donorUsername, transactionId);
            MoneyTransferResponse moneyTransferResponse = transferMoneyBetweenAccounts(transaction,
                    transactionMapper.toMoneyTransferRequest(transaction));
            moneyTransferResponseList.add(moneyTransferResponse);
        }

        return moneyTransferResponseList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void declinePendingRequest(String donorUsername, TransactionsListDto transactionsList) {
        List<UUID> validIds = extractValidUUIDs(transactionsList.getIds());

        for (UUID transactionId : validIds) {
            transactionRepository.declineTransaction(donorUsername, transactionId);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<TransactionDto> getHistory(String login, PlayerAction action) {
        Collection<Transaction> transactionsByUser;

        if (action == null) {
            transactionsByUser = transactionRepository
                    .getTransactionsBySenderAndRecipientAndStatus(login, null, null);
            transactionsByUser.addAll(transactionRepository
                    .getTransactionsBySenderAndRecipientAndStatus(null, login, null));

            return transactionsByUser.stream()
                    .map(transactionMapper::toTransactionDto)
                    .collect(Collectors.toList());
        }

        switch (action) {
            case DEBIT -> transactionsByUser = transactionRepository
                    .getTransactionsBySenderAndRecipientAndStatus(login, null, null);
            case CREDIT -> transactionsByUser = transactionRepository
                    .getTransactionsBySenderAndRecipientAndStatus(null, login, null);
            default -> throw new RuntimeException("Unknown exception while getting history");
        }

        return transactionsByUser.stream()
                .map(transactionMapper::toTransactionDto)
                .collect(Collectors.toList());
    }

    /**
     * Метод, отвественный за перевод денег между двумя пользователями
     *
     * @param transaction          открытая транзакция
     * @param moneyTransferRequest обертка над запросом денежных средств
     */
    private MoneyTransferResponse transferMoneyBetweenAccounts(Transaction transaction,
                                                               MoneyTransferRequest moneyTransferRequest) {
        Player sender = playerRepository.getByLogin(moneyTransferRequest.getMoneyFrom());
        Player recipient = playerRepository.getByLogin(moneyTransferRequest.getMoneyTo());

        BigDecimal balanceAfterMoneyWithdrawal = sender.getBalance().subtract(moneyTransferRequest.getAmount());

        if (balanceAfterMoneyWithdrawal.signum() < 0) {
            transactionRepository.setFailed(transaction.getId());
            transaction.setStatus(TransferRequestStatus.FAILED);
            throw new DeficientBalanceException(
                    String.format("Не хватает деньги на балансе игрока с id=%d", sender.getId())
            );
        }

        sender = playerRepository.setBalance(sender.getLogin(), balanceAfterMoneyWithdrawal);
        recipient = playerRepository.setBalance(recipient.getLogin(),
                recipient.getBalance().add(moneyTransferRequest.getAmount()));

        transaction.setStatus(TransferRequestStatus.APPROVED);
        Transaction approvedTransaction =
                transactionRepository.approveTransaction(sender.getLogin(), transaction.getId());

        return new MoneyTransferResponse(
                playerMapper.toAuthenticatedPlayerDto(sender),
                transactionMapper.toTransactionDto(approvedTransaction)
        );
    }

    /**
     * Метод читает коллекцию UUID в строковом представлении, и возвращает список UUID, игнорируя невалидные строковые
     * представления UUID
     */
    private List<UUID> extractValidUUIDs(Collection<String> ids) {
        List<UUID> result = new ArrayList<>(ids.size());

        for (String id : ids) {
            try {
                result.add(UUID.fromString(id));
            } catch (IllegalArgumentException e) {
            }
        }
        return result;
    }
}
