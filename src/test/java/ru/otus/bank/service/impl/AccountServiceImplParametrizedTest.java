package ru.otus.bank.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.bank.dao.AccountDao;
import ru.otus.bank.entity.Account;
import ru.otus.bank.entity.Agreement;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccountServiceImplParametrizedTest {
    @Mock
    AccountDao accountDao;

    @InjectMocks
    AccountServiceImpl accountServiceImpl;

    @ParameterizedTest
    @CsvSource({"100, 10, true", "10, 100, false", "10, 0, false", "10, -1, false"})
    public void testTransferValidation(String sourceSum, String transferSum, String expectedResult) {
        BigDecimal sourceAmount = new BigDecimal(sourceSum);
        BigDecimal transferAmount = new BigDecimal(transferSum);
        Boolean expected = Boolean.parseBoolean(expectedResult);

        Account sourceAccount = new Account();
        sourceAccount.setAmount(sourceAmount);
        sourceAccount.setId(1L);

        Account destinationAccount = new Account();
        destinationAccount.setAmount(new BigDecimal(10));
        destinationAccount.setId(2L);

        when(accountDao.findById(eq(1L))).thenReturn(Optional.of(sourceAccount));
        when(accountDao.findById(eq(2L))).thenReturn(Optional.of(destinationAccount));

        assertEquals(expected, accountServiceImpl.makeTransfer(1L, 2L, transferAmount));
        }

    @ParameterizedTest
    @MethodSource("provideParameters")
    public void testTransferValidationMethodSource(BigDecimal sourceAmount, BigDecimal transferAmount, Boolean expected) {
        Account sourceAccount = new Account();
        sourceAccount.setAmount(sourceAmount);
        sourceAccount.setId(1L);

        Account destinationAccount = new Account();
        destinationAccount.setAmount(new BigDecimal(10));
        destinationAccount.setId(2L);

        when(accountDao.findById(eq(1L))).thenReturn(Optional.of(sourceAccount));
        when(accountDao.findById(eq(2L))).thenReturn(Optional.of(destinationAccount));

        assertEquals(expected, accountServiceImpl.makeTransfer(1L, 2L, transferAmount));
    }

    public static Stream<? extends Arguments> provideParameters() {
        return Stream.of(
            Arguments.of(new BigDecimal(100), new BigDecimal(10), true),
            Arguments.of(new BigDecimal(10), new BigDecimal(100), false),
            Arguments.of(new BigDecimal(100), new BigDecimal(0), false),
            Arguments.of(new BigDecimal(100), new BigDecimal(-1), false)
        );
    }

    @ParameterizedTest
    @CsvSource({"123456, 100", "1111, 200"})
    public void testAddAccount(String accountNumber, String amountStr) {
        BigDecimal amount = new BigDecimal(amountStr);
        Agreement agreement = new Agreement();
        agreement.setId(1L);

        Account account = new Account();
        account.setAgreementId(agreement.getId());
        account.setNumber(accountNumber);
        account.setType(1);
        account.setAmount(amount);

        when(accountDao.save(any(Account.class))).thenReturn(account);

        Account result = accountServiceImpl.addAccount(agreement, accountNumber, 1, amount);

        ArgumentMatcher<Account> accountMatcher =
                argument -> argument.getAgreementId().equals(agreement.getId()) &&
                        argument.getNumber().equals(accountNumber) &&
                        argument.getType().equals(1) &&
                        argument.getAmount().equals(amount);


        assertEquals(agreement.getId(), result.getAgreementId());
        assertEquals(accountNumber, result.getNumber());
        assertEquals(1, result.getType());
        assertEquals(amount, result.getAmount());
        verify(accountDao).save(argThat(accountMatcher));
    }
}
