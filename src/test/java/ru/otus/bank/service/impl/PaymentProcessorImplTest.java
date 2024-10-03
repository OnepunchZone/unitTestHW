package ru.otus.bank.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.bank.entity.Account;
import ru.otus.bank.entity.Agreement;
import ru.otus.bank.service.AccountService;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentProcessorImplTest {

    @Mock
    AccountService accountService;

    @InjectMocks
    PaymentProcessorImpl paymentProcessor;

    @Test
    public void testTransfer() {
        Agreement sourceAgreement = new Agreement();
        sourceAgreement.setId(1L);

        Agreement destinationAgreement = new Agreement();
        destinationAgreement.setId(2L);

        Account sourceAccount = new Account();
        sourceAccount.setAmount(BigDecimal.TEN);
        sourceAccount.setType(0);

        Account destinationAccount = new Account();
        destinationAccount.setAmount(BigDecimal.ZERO);
        destinationAccount.setType(0);

        when(accountService.getAccounts(argThat(new ArgumentMatcher<Agreement>() {
            @Override
            public boolean matches(Agreement argument) {
                return argument != null && argument.getId() == 1L;
            }
        }))).thenReturn(List.of(sourceAccount));

        when(accountService.getAccounts(argThat(new ArgumentMatcher<Agreement>() {
            @Override
            public boolean matches(Agreement argument) {
                return argument != null && argument.getId() == 2L;
            }
        }))).thenReturn(List.of(destinationAccount));

        paymentProcessor.makeTransfer(sourceAgreement, destinationAgreement,
                0, 0, BigDecimal.ONE);

    }

    @Test
    public void testMakeTransferWithCommission() {
        Agreement srcAgreement = new Agreement();
        Agreement destAgreement = new Agreement();

        Account srcAccount = new Account();
        srcAccount.setId(1L);
        srcAccount.setType(1);
        srcAccount.setAmount(new BigDecimal(1000));

        Account destAccount = new Account();
        destAccount.setId(2L);
        destAccount.setType(1);

        BigDecimal amount = new BigDecimal(100);
        BigDecimal percent = new BigDecimal("0.05");

        when(accountService.getAccounts(srcAgreement)).thenReturn(List.of(srcAccount));
        when(accountService.getAccounts(destAgreement)).thenReturn(List.of(destAccount));
        when(accountService.makeTransfer(anyLong(), anyLong(), eq(amount))).thenReturn(true);
        when(accountService.charge(anyLong(), any(BigDecimal.class))).thenReturn(true);

        boolean result = paymentProcessor.makeTransferWithComission(
                srcAgreement, destAgreement, 1, 1, amount, percent
        );

        assertTrue(result);

        BigDecimal expectedCommission = amount.negate().multiply(percent);
        verify(accountService).charge(eq(srcAccount.getId()), eq(expectedCommission));

        verify(accountService).makeTransfer(eq(srcAccount.getId()), eq(destAccount.getId()), eq(amount));
    }

}
