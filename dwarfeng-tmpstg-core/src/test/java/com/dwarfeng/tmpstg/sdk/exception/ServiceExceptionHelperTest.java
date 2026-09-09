package com.dwarfeng.tmpstg.sdk.exception;

import com.dwarfeng.subgrade.basic.impl.exception.MapServiceExceptionMapper;
import com.dwarfeng.subgrade.basic.stack.exception.ServiceException;
import com.dwarfeng.tmpstg.stack.exception.TemporaryStorageException;
import com.dwarfeng.tmpstg.stack.exception.TemporaryStorageHandlerNotFoundException;
import com.dwarfeng.tmpstg.stack.exception.TemporaryStorageQosException;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link ServiceExceptionHelper} 的单元测试。
 */
class ServiceExceptionHelperTest {

    private static ServiceException.Code code(int code, String tip) {
        return new ServiceException.Code(code, tip);
    }

    @Test
    void shouldProvideDestinationsUsableByMapper() {
        Map<Class<? extends Exception>, Supplier<ServiceException.Code>> destination =
                ServiceExceptionHelper.putDefaultDestination(null);
        MapServiceExceptionMapper mapper = new MapServiceExceptionMapper(
                destination, () -> new ServiceException.Code(-1, "default")
        );

        ServiceException mappedException = mapper.map(new TemporaryStorageHandlerNotFoundException("missing"));
        ServiceException.Code expectedCode =
                ServiceExceptionCodeSuppliers.TEMPORARY_STORAGE_QOS_HANDLER_NOT_FOUND.get();

        assertEquals(expectedCode.getCode(), mappedException.getCode().getCode());
        assertEquals(expectedCode.getTip(), mappedException.getCode().getTip());
    }

    @Test
    void shouldResolveDirectDestinationForEveryMapping() {
        AtomicInteger sequence = new AtomicInteger();
        Map<Class<? extends Exception>, Supplier<ServiceException.Code>> destination = new HashMap<>();
        destination.put(TemporaryStorageException.class, () -> code(sequence.incrementAndGet(), "direct"));
        MapServiceExceptionMapper mapper = new MapServiceExceptionMapper(destination, () -> code(-1, "default"));

        assertEquals(1, mapper.map(new TemporaryStorageException()).getCode().getCode());
        assertEquals(2, mapper.map(new TemporaryStorageException()).getCode().getCode());
    }

    @Test
    void shouldResolveCachedParentDestinationForEveryMapping() {
        AtomicInteger sequence = new AtomicInteger();
        Map<Class<? extends Exception>, Supplier<ServiceException.Code>> destination = new HashMap<>();
        destination.put(TemporaryStorageQosException.class, () -> code(sequence.incrementAndGet(), "parent"));
        MapServiceExceptionMapper mapper = new MapServiceExceptionMapper(destination, () -> code(-1, "default"));

        assertEquals(1, mapper.map(new TemporaryStorageHandlerNotFoundException("missing")).getCode().getCode());
        assertEquals(2, mapper.map(new TemporaryStorageHandlerNotFoundException("missing")).getCode().getCode());
    }

    @Test
    void shouldResolveDefaultDestinationForEveryMapping() {
        AtomicInteger sequence = new AtomicInteger();
        MapServiceExceptionMapper mapper = new MapServiceExceptionMapper(
                new HashMap<>(), () -> code(sequence.incrementAndGet(), "default")
        );

        assertEquals(1, mapper.map(new Exception()).getCode().getCode());
        assertEquals(2, mapper.map(new Exception()).getCode().getCode());
    }
}
