package aop;

import application.model.dto.AuthenticationDto;
import application.model.dto.AuthenticationRequest;
import application.model.dto.BalanceDto;
import domain.dto.MoneyTransferResponse;
import domain.dto.PlayerCreationRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import service.PlayerAction;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.UUID;

import static logging.LoggerFactory.getLogger;

@Aspect
public class ControllerAuditAspect {
    @Pointcut("@within(aop.annotations.Audit)")
    public void annotatedByAuditOnClassLevel() {
    }

    @Around("annotatedByAuditOnClassLevel() " +
            "&& execution(* registerUser(*)) " +
            "&& args(request)")
    public Object registerUserMethod(ProceedingJoinPoint pjp, PlayerCreationRequest request)
            throws Throwable {
        try {
            getLogger().info(String.format("Регистрация пользователя с данными: login=%s username=%s password=%s",
                    request.getLogin(), request.getUsername(),
                    Arrays.toString(request.getPassword())));
            AuthenticationDto dto = (AuthenticationDto) pjp.proceed();
            getLogger().info(String.format("Пользователь зарегистрирован: id=%s, login=%s, username=%s, sessionId=%s, " +
                            "balance=%s", dto.getId(), dto.getLogin(), dto.getUsername(), dto.getSessionId(),
                    dto.getBalance()));
            return dto;
        } catch (Throwable e) {
            getLogger().warn(e.getMessage());
            throw e;
        }
    }

    @Around("annotatedByAuditOnClassLevel() " +
            "&& execution(* authenticate(*)) " +
            "&& args(authenticationRequest)")
    public Object authenticateUserMethod(ProceedingJoinPoint pjp, AuthenticationRequest authenticationRequest)
            throws Throwable {
        try {
            getLogger().info(String.format("Авторизация пользователя с данными: login=%s password=%s",
                    authenticationRequest.getLogin(), Arrays.toString(authenticationRequest.getPassword())));
            AuthenticationDto dto = (AuthenticationDto) pjp.proceed();
            getLogger().info(String.format("Пользователь авторизован: id=%s, login=%s, username=%s, sessionId=%s, " +
                            "balance=%s", dto.getId(), dto.getLogin(), dto.getUsername(), dto.getSessionId(),
                    dto.getBalance()));
            return dto;
        } catch (Throwable e) {
            getLogger().warn(e.getMessage());
            throw e;
        }
    }

    @Around("annotatedByAuditOnClassLevel() " +
            "&& execution(* getBalance(*,*)) " +
            "&& args(id,sessionId)")
    public Object balanceMethod(ProceedingJoinPoint pjp, Long id, UUID sessionId) throws Throwable {
        try {
            getLogger().info(String.format("Запрос баланса user_id=%s session_id=%s",
                    id, sessionId));
            BalanceDto dto = (BalanceDto) pjp.proceed();
            getLogger().info(String.format("Запрос баланса обработан: id=%s, username=%s, balance=%s",
                    dto.getId(), dto.getUsername(), dto.getBalance()));
            return dto;
        } catch (Throwable e) {
            getLogger().warn(e.getMessage());
            throw e;
        }
    }

    @Around(value = "annotatedByAuditOnClassLevel() " +
            "&& execution(* transferMoney(*,*,*,*,*)) " +
            "&& args(sender,recipient,amount,sessionId,transactionId)")
    public Object moneyTransferMethod(ProceedingJoinPoint pjp, String sender, String recipient, BigDecimal amount,
                                      UUID sessionId, UUID transactionId) throws Throwable {
        try {
            getLogger().info(String.format("Инициация перевода денег от username=%s к username=%s суммой=%s. " +
                            "session_id=%s transaction_id=%s",
                    sender, recipient, amount, sessionId, transactionId));
            BalanceDto dto = (BalanceDto) pjp.proceed();
            getLogger().info(String.format("Запрос баланса обработан: id=%s, username=%s, balance=%s",
                    dto.getId(), dto.getUsername(), dto.getBalance()));
            return dto;
        } catch (Throwable e) {
            getLogger().warn(e.getMessage());
            throw e;
        }
    }

    @Around("annotatedByAuditOnClassLevel() " +
            "&& execution(* requestMoneyFrom(*,*,*,*,*)) " +
            "&& args(requester,donor,amount,sessionId,transactionId)")
    public Object requestMoneyMethod(ProceedingJoinPoint pjp, String requester, String donor, BigDecimal amount,
                                     UUID sessionId, UUID transactionId) throws Throwable {
        try {
            getLogger().info(String.format("Запрос денег исходящий от username=%s к username=%s суммой=%s. session_id=%s " +
                    "transaction_id=%s", requester, donor, amount, sessionId, transactionId));
            return pjp.proceed();
        } catch (Throwable e) {
            getLogger().warn(e.getMessage());
            throw e;
        }
    }

    @Around("annotatedByAuditOnClassLevel() " +
            "&& execution(* getPendingMoneyRequests(*,*)) " +
            "&& args(login,sessionId)")
    public Object pendingRequestsMethod(ProceedingJoinPoint pjp, String login, UUID sessionId) throws Throwable {
        try {
            getLogger().info(String.format("Пользователь username=%s запросил список транзакций, ожидающих подтверждения. " +
                    "session_id=%s", login, sessionId));
            return pjp.proceed();
        } catch (Throwable e) {
            getLogger().warn(e.getMessage());
            throw e;
        }
    }

    @Around("annotatedByAuditOnClassLevel() " +
            "&& execution(* getHistory(*,*,*)) " +
            "&& args(login,action,sessionId)")
    public Object historyMethod(ProceedingJoinPoint pjp, String login, PlayerAction action, UUID sessionId)
            throws Throwable {
        try {
            getLogger().info(String.format("Пользователь login=%s запросил историю транзакций типа player_action=%s. " +
                    "session_id=%s", login, action, sessionId));
            return pjp.proceed();
        } catch (Throwable e) {
            getLogger().warn(e.getMessage());
            throw e;
        }
    }

    @Around("annotatedByAuditOnClassLevel() " +
            "&& execution(* approvePendingRequest(*,*,*)) " +
            "&& args(sessionId,donorUsername,transactionId)")
    public Object approveMoneyRequestMethod(ProceedingJoinPoint pjp, UUID sessionId, String donorUsername,
                                            UUID transactionId) throws Throwable {
        try {
            getLogger().info(String.format("Пользователь username=%s пытается подтвердить транзакцию transaction_id=%s. " +
                    "session_id=%s", donorUsername, transactionId, sessionId));
            MoneyTransferResponse response = (MoneyTransferResponse) pjp.proceed();
            getLogger().info(String.format("Транзакция пользователя username=%s подтверждена transaction_id=%s, session_id=%s",
                    donorUsername, transactionId, sessionId));
            return response;
        } catch (Throwable e) {
            getLogger().warn(e.getMessage());
            throw e;
        }
    }

    @Around("annotatedByAuditOnClassLevel() " +
            "&& execution(* declinePendingRequest(*,*,*)) " +
            "&& args(sessionId,donorUsername,transactionId)")
    public Object declineMoneyRequestMethod(ProceedingJoinPoint pjp, UUID sessionId, String donorUsername,
                                            UUID transactionId) throws Throwable {
        try {
            getLogger().info(String.format("Пользователь username=%s пытается отклонить транзакцию transaction_id=%s. " +
                    "session_id=%s", donorUsername, transactionId, sessionId));
            Object result = pjp.proceed();
            getLogger().info(String.format("Транзакция пользователя username=%s отклонена transaction_id=%s, session_id=%s",
                    donorUsername, transactionId, sessionId));
            return result;
        } catch (Throwable e) {
            getLogger().warn(e.getMessage());
            throw e;
        }
    }

    @Around("annotatedByAuditOnClassLevel() " +
            "&& execution(* signOut(*,*)) " +
            "&& args(username,sessionId)")
    public Object signOutMethod(ProceedingJoinPoint pjp, String username, UUID sessionId) throws Throwable {
        try {
            Object object = pjp.proceed();
            getLogger().info(String.format("Пользователь username=%s закрывает сессию session_id=%s",
                    username, sessionId));
            return object;
        } catch (Throwable e) {
            getLogger().warn(e.getMessage());
            throw e;
        }
    }
}
