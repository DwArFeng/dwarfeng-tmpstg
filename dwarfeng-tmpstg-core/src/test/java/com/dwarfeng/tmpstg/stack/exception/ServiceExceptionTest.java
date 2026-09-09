package com.dwarfeng.tmpstg.stack.exception;

import com.dwarfeng.subgrade.basic.stack.exception.ServiceException;
import com.dwarfeng.tmpstg.sdk.exception.ServiceExceptionCodeSuppliers;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link ServiceException} 序列化行为的单元测试。
 */
class ServiceExceptionTest {

    @Test
    void shouldPreserveCodeTipAndMessageAfterSerializationRoundTrip() throws Exception {
        ServiceException originalException = new ServiceException(
                ServiceExceptionCodeSuppliers.TEMPORARY_STORAGE_FAILED.get()
        );

        byte[] serialized;
        try (
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)
        ) {
            objectOutputStream.writeObject(originalException);
            objectOutputStream.flush();
            serialized = byteArrayOutputStream.toByteArray();
        }

        ServiceException restoredException;
        try (
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(serialized);
                ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)
        ) {
            restoredException = (ServiceException) objectInputStream.readObject();
        }

        assertEquals(originalException.getCode().getCode(), restoredException.getCode().getCode());
        assertEquals(originalException.getCode().getTip(), restoredException.getCode().getTip());
        assertEquals(originalException.getMessage(), restoredException.getMessage());
    }
}
