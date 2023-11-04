package ru.tonychem.aop;

import lombok.Setter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.tonychem.domain.dto.AuthenticatedPlayerDto;
import ru.tonychem.domain.dto.BalanceDto;
import ru.tonychem.in.dto.*;
import ru.tonychem.logging.Logger;
import ru.tonychem.service.PlayerAction;

/**
 * Аспект, ответственный за логгирование всех методово контроллера
 */
@Aspect
@Component
@Setter
public class ControllerAuditAspect {

    private Logger logger;

    @Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
    public void restController() {
    }

    @Pointcut("@within(ru.tonychem.aop.annotations.Audit)")
    public void annotatedByAuditOnClassLevel() {
    }

    @Around("restController()" +
            "&& annotatedByAuditOnClassLevel() " +
            "&& execution(* registerPlayer(*)) " +
            "&& args(request)")
    public Object registerUserMethod(ProceedingJoinPoint pjp, UnsecuredPlayerCreationRequestDto request)
            throws Throwable {
        try {
            logger.info(String.format("Регистрация пользователя с данными: login=%s username=%s password=%s",
                    request.getLogin(), request.getUsername(), request.getPassword()));
            Object response = pjp.proceed();

            ResponseEntity responseEntity = (ResponseEntity) response;
            AuthenticatedPlayerDto dto = (AuthenticatedPlayerDto) responseEntity.getBody();
            logger.info(String.format("Пользователь зарегистрирован: id=%s, login=%s, username=%s " +
                    "balance=%s", dto.getId(), dto.getLogin(), dto.getUsername(), dto.getBalance()));
            return response;
        } catch (Throwable e) {
            logger.warn(e.getMessage());
            throw e;
        }
    }

    @Around("restController() " +
            "&& annotatedByAuditOnClassLevel() " +
            "&& execution(* authenticate(*)) " +
            "&& args(authenticationRequest)")
    public Object authenticateUserMethod(ProceedingJoinPoint pjp, UnsecuredAuthenticationRequestDto authenticationRequest)
            throws Throwable {
        try {
            logger.info(String.format("Авторизация пользователя с данными: login=%s password=%s",
                    authenticationRequest.getLogin(), authenticationRequest.getPassword()));
            Object response = pjp.proceed();

            ResponseEntity responseEntity = (ResponseEntity) response;
            AuthenticatedPlayerDto dto = (AuthenticatedPlayerDto) responseEntity.getBody();
            logger.info(String.format("Пользователь авторизован: id=%s, login=%s, username=%s " +
                    "balance=%s", dto.getId(), dto.getLogin(), dto.getUsername(), dto.getBalance()));
            return response;
        } catch (Throwable e) {
            logger.warn(e.getMessage());
            throw e;
        }
    }

    @Around("restController() " +
            "&& annotatedByAuditOnClassLevel() " +
            "&& execution(* getBalance(*)) " +
            "&& args(authToken)")
    public Object balanceMethod(ProceedingJoinPoint pjp, String authToken) throws Throwable {
        try {
            logger.info(String.format("Запрос баланса authToken = %s", authToken));
            Object response = pjp.proceed();

            ResponseEntity responseEntity = (ResponseEntity) response;
            BalanceDto dto = (BalanceDto) responseEntity.getBody();
            logger.info(String.format("Запрос баланса обработан: id=%s, username=%s, balance=%s",
                    dto.getId(), dto.getUsername(), dto.getBalance()));
            return response;
        } catch (Throwable e) {
            logger.warn(e.getMessage());
            throw e;
        }
    }

    @Around("restController()" +
            "&& annotatedByAuditOnClassLevel() " +
            "&& execution(* transferMoney(*,*)) " +
            "&& args(authToken,moneyRequest)")
    public Object moneyTransferMethod(ProceedingJoinPoint pjp, String authToken, PlayerTransferMoneyRequestDto moneyRequest)
            throws Throwable {
        try {
            logger.info(String.format("Инициация перевода денег к username=%s суммой=%f. " +
                            "authToken = %s",
                    moneyRequest.getRecipient(), moneyRequest.getAmount(), authToken));
            Object response = pjp.proceed();

            ResponseEntity responseEntity = (ResponseEntity) response;
            BalanceDto dto = (BalanceDto) responseEntity.getBody();
            logger.info(String.format("Запрос баланса обработан: id=%s, username=%s, balance=%s",
                    dto.getId(), dto.getUsername(), dto.getBalance()));
            return response;
        } catch (Throwable e) {
            logger.warn(e.getMessage());
            throw e;
        }
    }

    @Around("restController() " +
            "&& annotatedByAuditOnClassLevel() " +
            "&& execution(* requestMoney(*,*)) " +
            "&& args(authToken,playerRequestMoneyDto)")
    public Object requestMoneyMethod(ProceedingJoinPoint pjp, String authToken, PlayerRequestMoneyDto playerRequestMoneyDto)
            throws Throwable {
        try {
            logger.info(String.format("Запрос денег к username=%s суммой=%s. authToken=%s",
                    playerRequestMoneyDto.getDonor(), playerRequestMoneyDto.getAmount(), authToken));
            return pjp.proceed();
        } catch (Throwable e) {
            logger.warn(e.getMessage());
            throw e;
        }
    }

    @Around("restController() " +
            "&& annotatedByAuditOnClassLevel() " +
            "&& execution(* getPendingMoneyRequests(*)) " +
            "&& args(authToken)")
    public Object pendingRequestsMethod(ProceedingJoinPoint pjp, String authToken) throws Throwable {
        try {
            logger.info(String.format("Пользователь запросил список транзакций, ожидающих подтверждения. " +
                    "authToken=%s", authToken));
            return pjp.proceed();
        } catch (Throwable e) {
            logger.warn(e.getMessage());
            throw e;
        }
    }

    @Around("restController() " +
            "&& annotatedByAuditOnClassLevel() " +
            "&& execution(* getHistory(*,*,*)) " +
            "&& args(authToken,action)")
    public Object historyMethod(ProceedingJoinPoint pjp, String authToken, PlayerAction action)
            throws Throwable {
        try {
            logger.info(String.format("Пользователь запросил историю транзакций типа player_action=%s. " +
                    "authToken=%s", action, authToken));
            return pjp.proceed();
        } catch (Throwable e) {
            logger.warn(e.getMessage());
            throw e;
        }
    }

    @Around("restController() " +
            "&& annotatedByAuditOnClassLevel() " +
            "&& execution(* approvePendingMoneyRequests(*,*)) " +
            "&& args(authToken,transactionsListDto)")
    public Object approveMoneyRequestMethod(ProceedingJoinPoint pjp, String authToken,
                                            TransactionsListDto transactionsListDto)
            throws Throwable {
        try {
            logger.info(String.format("Пользователь пытается подтвердить транзакции transactionList=%s. " +
                    "authToken=%s", transactionsListDto, authToken));
            return pjp.proceed();
        } catch (Throwable e) {
            logger.warn(e.getMessage());
            throw e;
        }
    }

    @Around("restController() " +
            "&& annotatedByAuditOnClassLevel() " +
            "&& execution(* declinePendingMoneyRequests(*,*)) " +
            "&& args(authToken,transactionsListDto)")
    public Object declineMoneyRequestMethod(ProceedingJoinPoint pjp, String authToken,
                                            TransactionsListDto transactionsListDto) throws Throwable {
        try {
            logger.info(String.format("Пользователь пытается отклонить транзакции transactionList=%s. " +
                    "authToken=%s", transactionsListDto, authToken));
            return pjp.proceed();
        } catch (Throwable e) {
            logger.warn(e.getMessage());
            throw e;
        }
    }

    @Around("restController() " +
            "&& annotatedByAuditOnClassLevel() " +
            "&& execution(* logout(*)) " +
            "&& args(authToken)")
    public Object signOutMethod(ProceedingJoinPoint pjp, String authToken) throws Throwable {
        try {
            Object object = pjp.proceed();
            logger.info(String.format("Пользователь закрывает сессию authToken=%s",
                    authToken));
            return object;
        } catch (Throwable e) {
            logger.warn(e.getMessage());
            throw e;
        }
    }
}