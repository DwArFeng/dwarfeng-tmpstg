package com.dwarfeng.tmpstg.sdk.exception;

import com.dwarfeng.subgrade.basic.stack.exception.ServiceException;
import com.dwarfeng.tmpstg.base.sdk.i18n.MessageContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

/**
 * {@link ServiceExceptionCodeSuppliers} 的单元测试。
 */
class ServiceExceptionCodeSuppliersTest {

    private static final int DEFAULT_EXCEPTION_CODE_OFFSET = 20000;

    @AfterEach
    void tearDown() {
        ServiceExceptionCodeSuppliers.setExceptionCodeOffset(DEFAULT_EXCEPTION_CODE_OFFSET);
    }

    @Test
    void shouldResolveTipFromCurrentMessageContext() {
        ServiceException.Code englishCode = MessageContext.call(
                Locale.ENGLISH, ServiceExceptionCodeSuppliers.TEMPORARY_STORAGE_FAILED::get
        );
        ServiceException.Code chineseCode = MessageContext.call(
                Locale.SIMPLIFIED_CHINESE, ServiceExceptionCodeSuppliers.TEMPORARY_STORAGE_FAILED::get
        );

        assertEquals("temporary storage failed", englishCode.getTip());
        assertEquals("临时存储异常", chineseCode.getTip());
        assertNotSame(englishCode, chineseCode);
    }

    @Test
    void shouldOnlyApplyOffsetToNewCodeSnapshots() {
        ServiceException.Code originalCode = ServiceExceptionCodeSuppliers.TEMPORARY_STORAGE_FAILED.get();

        ServiceExceptionCodeSuppliers.setExceptionCodeOffset(DEFAULT_EXCEPTION_CODE_OFFSET + 1000);
        ServiceException.Code updatedCode = ServiceExceptionCodeSuppliers.TEMPORARY_STORAGE_FAILED.get();

        assertEquals(DEFAULT_EXCEPTION_CODE_OFFSET, originalCode.getCode());
        assertEquals(DEFAULT_EXCEPTION_CODE_OFFSET + 1000, updatedCode.getCode());
        assertNotSame(originalCode, updatedCode);
    }
}
