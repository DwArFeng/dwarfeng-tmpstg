package com.dwarfeng.tmpstg.impl.handler;

import com.dwarfeng.subgrade.sdk.exception.HandlerExceptionHelper;
import com.dwarfeng.subgrade.stack.exception.HandlerException;
import com.dwarfeng.tmpstg.stack.bean.dto.TemporaryStorageInfo;
import com.dwarfeng.tmpstg.stack.exception.AmbiguousTemporaryStorageHandlerException;
import com.dwarfeng.tmpstg.stack.exception.NoTemporaryStorageHandlerPresentException;
import com.dwarfeng.tmpstg.stack.exception.TemporaryStorageHandlerNotFoundException;
import com.dwarfeng.tmpstg.stack.handler.TemporaryStorageHandler;
import com.dwarfeng.tmpstg.stack.handler.TemporaryStorageQosHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 临时存储 QoS 处理器实现。
 *
 * @author DwArFeng
 * @since 2.0.0
 */
public class TemporaryStorageQosHandlerImpl implements TemporaryStorageQosHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TemporaryStorageQosHandlerImpl.class);

    private final Map<String, TemporaryStorageHandler> temporaryStorageHandlerMap;

    public TemporaryStorageQosHandlerImpl(Map<String, TemporaryStorageHandler> temporaryStorageHandlerMap) {
        this.temporaryStorageHandlerMap = Optional.ofNullable(temporaryStorageHandlerMap)
                .orElse(Collections.emptyMap());
    }

    @Override
    public List<String> listHandlerNames() throws HandlerException {
        try {
            List<String> handlerNames = temporaryStorageHandlerMap.keySet().stream()
                    .sorted()
                    .collect(Collectors.toList());
            return Collections.unmodifiableList(handlerNames);
        } catch (Exception e) {
            throw HandlerExceptionHelper.parse(e);
        }
    }

    @Override
    public void stopAllManagedHandlers() throws HandlerException {
        try {
            temporaryStorageHandlerMap.keySet().stream().sorted().forEach(name -> {
                TemporaryStorageHandler handler = temporaryStorageHandlerMap.get(name);
                try {
                    handler.stop();
                } catch (Exception e) {
                    LOGGER.warn("停止临时存储处理器 {} 失败，将继续尝试停止其余处理器", name, e);
                }
            });
        } catch (Exception e) {
            throw HandlerExceptionHelper.parse(e);
        }
    }

    @Override
    public boolean isStarted(@Nullable String handlerName) throws HandlerException {
        try {
            return determineHandler(handlerName).isStarted();
        } catch (Exception e) {
            throw HandlerExceptionHelper.parse(e);
        }
    }

    @Override
    public void start(@Nullable String handlerName) throws HandlerException {
        try {
            determineHandler(handlerName).start();
        } catch (Exception e) {
            throw HandlerExceptionHelper.parse(e);
        }
    }

    @Override
    public void stop(@Nullable String handlerName) throws HandlerException {
        try {
            determineHandler(handlerName).stop();
        } catch (Exception e) {
            throw HandlerExceptionHelper.parse(e);
        }
    }

    @Override
    public Collection<String> keys(@Nullable String handlerName) throws HandlerException {
        try {
            return determineHandler(handlerName).keys();
        } catch (Exception e) {
            throw HandlerExceptionHelper.parse(e);
        }
    }

    @Override
    public boolean exists(@Nullable String handlerName, @Nonnull String key) throws HandlerException {
        try {
            return determineHandler(handlerName).exists(key);
        } catch (Exception e) {
            throw HandlerExceptionHelper.parse(e);
        }
    }

    @Override
    public TemporaryStorageInfo inspect(@Nullable String handlerName, @Nonnull String key) throws HandlerException {
        try {
            return determineHandler(handlerName).inspect(key);
        } catch (Exception e) {
            throw HandlerExceptionHelper.parse(e);
        }
    }

    @Override
    public String create(@Nullable String handlerName) throws HandlerException {
        try {
            return determineHandler(handlerName).create();
        } catch (Exception e) {
            throw HandlerExceptionHelper.parse(e);
        }
    }

    @Override
    public InputStream openInputStream(@Nullable String handlerName, @Nonnull String key) throws HandlerException {
        try {
            return determineHandler(handlerName).openInputStream(key);
        } catch (Exception e) {
            throw HandlerExceptionHelper.parse(e);
        }
    }

    @Override
    public OutputStream openOutputStream(@Nullable String handlerName, @Nonnull String key) throws HandlerException {
        try {
            return determineHandler(handlerName).openOutputStream(key);
        } catch (Exception e) {
            throw HandlerExceptionHelper.parse(e);
        }
    }

    @Override
    public OutputStream openOutputStream(
            @Nullable String handlerName, @Nonnull String key, long expectedLength
    ) throws HandlerException {
        try {
            return determineHandler(handlerName).openOutputStream(key, expectedLength);
        } catch (Exception e) {
            throw HandlerExceptionHelper.parse(e);
        }
    }

    @Override
    public void dispose(@Nullable String handlerName, @Nonnull String key) throws HandlerException {
        try {
            determineHandler(handlerName).dispose(key);
        } catch (Exception e) {
            throw HandlerExceptionHelper.parse(e);
        }
    }

    @Override
    public void remove(@Nullable String handlerName, @Nonnull String key) throws HandlerException {
        try {
            determineHandler(handlerName).remove(key);
        } catch (Exception e) {
            throw HandlerExceptionHelper.parse(e);
        }
    }

    @Override
    public boolean removeIfDisposed(@Nullable String handlerName, @Nonnull String key) throws HandlerException {
        try {
            return determineHandler(handlerName).removeIfDisposed(key);
        } catch (Exception e) {
            throw HandlerExceptionHelper.parse(e);
        }
    }

    @Override
    public void disposeAndRemove(@Nullable String handlerName, @Nonnull String key) throws HandlerException {
        try {
            determineHandler(handlerName).disposeAndRemove(key);
        } catch (Exception e) {
            throw HandlerExceptionHelper.parse(e);
        }
    }

    @Override
    public void clearDisposed(@Nullable String handlerName) throws HandlerException {
        try {
            determineHandler(handlerName).clearDisposed();
        } catch (Exception e) {
            throw HandlerExceptionHelper.parse(e);
        }
    }

    private TemporaryStorageHandler determineHandler(@Nullable String handlerName) throws Exception {
        if (temporaryStorageHandlerMap.isEmpty()) {
            throw new NoTemporaryStorageHandlerPresentException();
        }
        if (handlerName == null) {
            if (temporaryStorageHandlerMap.size() == 1) {
                return temporaryStorageHandlerMap.values().iterator().next();
            } else {
                throw new AmbiguousTemporaryStorageHandlerException();
            }
        } else {
            if (!temporaryStorageHandlerMap.containsKey(handlerName)) {
                throw new TemporaryStorageHandlerNotFoundException(handlerName);
            }
            return temporaryStorageHandlerMap.get(handlerName);
        }
    }

    @Override
    public String toString() {
        return "TemporaryStorageQosHandlerImpl{" +
                "temporaryStorageHandlerMap=" + temporaryStorageHandlerMap +
                '}';
    }
}
