package com.dwarfeng.tmpstg.stack.handler;

import com.dwarfeng.subgrade.stack.exception.HandlerException;
import com.dwarfeng.tmpstg.stack.bean.dto.TemporaryStorageInfo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

/**
 * 临时存储 QoS 处理器。
 *
 * <p>
 * 参数 <code>handlerName</code> 为对应 {@link TemporaryStorageHandler} 实例的 <code>bean name</code>。<br>
 * 当应用上下文中只有一个 {@link TemporaryStorageHandler} 时，参数 <code>handlerName</code> 可以为 <code>null</code>。
 *
 * @author DwArFeng
 * @since 2.0.0
 */
public interface TemporaryStorageQosHandler {

    /**
     * 列出所有临时存储处理器名称。
     *
     * <p>
     * 返回结果按字典序排序且不可变。
     *
     * @return 所有处理器的名称组成的列表（按字典序排序，不可变）。
     * @throws HandlerException 处理器异常。
     */
    List<String> listHandlerNames() throws HandlerException;

    /**
     * 停止全部临时存储托管处理器。
     *
     * <p>
     * 按处理器名称的字典序依次调用各 {@link TemporaryStorageHandler} 的 {@link TemporaryStorageHandler#stop()}，
     * 适用于容器销毁等多实例停机场景。<br>
     * 若某个处理器停止失败，将记录告警并继续尝试停止其余处理器，不因单次失败中断整体流程；
     * 遍历映射本身的非预期错误仍会以 {@link HandlerException} 形式抛出。
     *
     * @throws HandlerException 处理器异常。
     */
    void stopAllManagedHandlers() throws HandlerException;

    /**
     * 查询临时存储处理器是否已启动。
     *
     * @param handlerName 处理器名称。
     * @return 目标处理器是否已启动。
     * @throws HandlerException 处理器异常。
     */
    boolean isStarted(@Nullable String handlerName) throws HandlerException;

    /**
     * 启动临时存储处理器。
     *
     * @param handlerName 处理器名称。
     * @throws HandlerException 处理器异常。
     */
    void start(@Nullable String handlerName) throws HandlerException;

    /**
     * 停止临时存储处理器。
     *
     * @param handlerName 处理器名称。
     * @throws HandlerException 处理器异常。
     */
    void stop(@Nullable String handlerName) throws HandlerException;

    /**
     * 获取临时存储处理器维护的临时存储的键集合。
     *
     * @param handlerName 处理器名称。
     * @return 临时存储处理器维护的临时存储的键集合。
     * @throws HandlerException 处理器异常。
     */
    Collection<String> keys(@Nullable String handlerName) throws HandlerException;

    /**
     * 判断指定的键是否存在。
     *
     * @param handlerName 处理器名称。
     * @param key         指定的键。
     * @return 指定的键是否存在。
     * @throws HandlerException 处理器异常。
     */
    boolean exists(@Nullable String handlerName, @Nonnull String key) throws HandlerException;

    /**
     * 获取指定键对应的临时存储信息。
     *
     * @param handlerName 处理器名称。
     * @param key         指定的键。
     * @return 指定键对应的临时存储信息。
     * @throws HandlerException 处理器异常。
     */
    TemporaryStorageInfo inspect(@Nullable String handlerName, @Nonnull String key) throws HandlerException;

    /**
     * 创建一个新的临时存储。
     *
     * @param handlerName 处理器名称。
     * @return 新创建的临时存储的键。
     * @throws HandlerException 处理器异常。
     */
    String create(@Nullable String handlerName) throws HandlerException;

    /**
     * 打开指定键对应的输入流。
     *
     * @param handlerName 处理器名称。
     * @param key         指定的键。
     * @return 指定键对应的输入流。
     * @throws HandlerException 处理器异常。
     */
    InputStream openInputStream(@Nullable String handlerName, @Nonnull String key) throws HandlerException;

    /**
     * 打开指定键对应的输出流。
     *
     * @param handlerName 处理器名称。
     * @param key         指定的键。
     * @return 指定键对应的输出流。
     * @throws HandlerException 处理器异常。
     * @see #openOutputStream(String, String, long)
     */
    OutputStream openOutputStream(@Nullable String handlerName, @Nonnull String key) throws HandlerException;

    /**
     * 打开指定键对应的输出流。
     *
     * @param handlerName    处理器名称。
     * @param key            指定的键。
     * @param expectedLength 期望的数据长度。
     * @return 指定键对应的输出流。
     * @throws HandlerException 处理器异常。
     * @see #openOutputStream(String, String)
     */
    OutputStream openOutputStream(
            @Nullable String handlerName, @Nonnull String key, long expectedLength
    ) throws HandlerException;

    /**
     * 释放指定键对应的临时存储。
     *
     * @param handlerName 处理器名称。
     * @param key         指定的键。
     * @throws HandlerException 处理器异常。
     */
    void dispose(@Nullable String handlerName, @Nonnull String key) throws HandlerException;

    /**
     * 移除指定键对应的临时存储。
     *
     * @param handlerName 处理器名称。
     * @param key         指定的键。
     * @throws HandlerException 处理器异常。
     */
    void remove(@Nullable String handlerName, @Nonnull String key) throws HandlerException;

    /**
     * 如果指定的键对应的临时存储已释放，则移除该临时存储。
     *
     * @param handlerName 处理器名称。
     * @param key         指定的键。
     * @return 如果移除成功，则返回 <code>true</code>，否则返回 <code>false</code>。
     * @throws HandlerException 处理器异常。
     */
    boolean removeIfDisposed(@Nullable String handlerName, @Nonnull String key) throws HandlerException;

    /**
     * 释放并移除指定键对应的临时存储。
     *
     * @param handlerName 处理器名称。
     * @param key         指定的键。
     * @throws HandlerException 处理器异常。
     */
    void disposeAndRemove(@Nullable String handlerName, @Nonnull String key) throws HandlerException;

    /**
     * 清除处理器中已释放的临时存储。
     *
     * @param handlerName 处理器名称。
     * @throws HandlerException 处理器异常。
     */
    void clearDisposed(@Nullable String handlerName) throws HandlerException;
}
