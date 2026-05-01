package com.dwarfeng.tmpstg.stack.service;

import com.dwarfeng.subgrade.stack.exception.ServiceException;
import com.dwarfeng.tmpstg.stack.bean.dto.TemporaryStorageInfo;
import com.dwarfeng.tmpstg.stack.handler.TemporaryStorageHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

/**
 * 临时存储 QoS 服务。
 *
 * <p>
 * 参数 <code>handlerName</code> 为对应 {@link TemporaryStorageHandler} 实例的 <code>bean name</code>。<br>
 * 当应用上下文中只有一个 {@link TemporaryStorageHandler} 时，参数 <code>handlerName</code> 可以为 <code>null</code>。
 *
 * @author DwArFeng
 * @since 2.0.0
 */
public interface TemporaryStorageQosService {

    /**
     * 列出所有临时存储处理器名称。
     *
     * <p>
     * 返回结果按字典序排序且不可变。
     *
     * @return 所有处理器的名称组成的列表（按字典序排序，不可变）。
     * @throws ServiceException 服务异常。
     */
    List<String> listHandlerNames() throws ServiceException;

    /**
     * 查询临时存储处理器是否已启动。
     *
     * @param handlerName 处理器名称。
     * @return 目标处理器是否已启动。
     * @throws ServiceException 服务异常。
     */
    boolean isStarted(@Nullable String handlerName) throws ServiceException;

    /**
     * 启动临时存储处理器。
     *
     * @param handlerName 处理器名称。
     * @throws ServiceException 服务异常。
     */
    void start(@Nullable String handlerName) throws ServiceException;

    /**
     * 停止临时存储处理器。
     *
     * @param handlerName 处理器名称。
     * @throws ServiceException 服务异常。
     */
    void stop(@Nullable String handlerName) throws ServiceException;

    /**
     * 获取临时存储处理器维护的临时存储的键集合。
     *
     * @param handlerName 处理器名称。
     * @return 临时存储处理器维护的临时存储的键集合。
     * @throws ServiceException 服务异常。
     */
    Collection<String> keys(@Nullable String handlerName) throws ServiceException;

    /**
     * 判断指定的键是否存在。
     *
     * @param handlerName 处理器名称。
     * @param key         指定的键。
     * @return 指定的键是否存在。
     * @throws ServiceException 服务异常。
     */
    boolean exists(@Nullable String handlerName, @Nonnull String key) throws ServiceException;

    /**
     * 获取指定键对应的临时存储信息。
     *
     * @param handlerName 处理器名称。
     * @param key         指定的键。
     * @return 指定键对应的临时存储信息。
     * @throws ServiceException 服务异常。
     */
    TemporaryStorageInfo inspect(@Nullable String handlerName, @Nonnull String key) throws ServiceException;

    /**
     * 创建一个新的临时存储。
     *
     * @param handlerName 处理器名称。
     * @return 新创建的临时存储的键。
     * @throws ServiceException 服务异常。
     */
    String create(@Nullable String handlerName) throws ServiceException;

    /**
     * 打开指定键对应的输入流。
     *
     * @param handlerName 处理器名称。
     * @param key         指定的键。
     * @return 指定键对应的输入流。
     * @throws ServiceException 服务异常。
     */
    InputStream openInputStream(@Nullable String handlerName, @Nonnull String key) throws ServiceException;

    /**
     * 打开指定键对应的输出流。
     *
     * @param handlerName 处理器名称。
     * @param key         指定的键。
     * @return 指定键对应的输出流。
     * @throws ServiceException 服务异常。
     * @see #openOutputStream(String, String, long)
     */
    OutputStream openOutputStream(@Nullable String handlerName, @Nonnull String key) throws ServiceException;

    /**
     * 打开指定键对应的输出流。
     *
     * @param handlerName    处理器名称。
     * @param key            指定的键。
     * @param expectedLength 期望的数据长度。
     * @return 指定键对应的输出流。
     * @throws ServiceException 服务异常。
     * @see #openOutputStream(String, String)
     */
    OutputStream openOutputStream(
            @Nullable String handlerName, @Nonnull String key, long expectedLength
    ) throws ServiceException;

    /**
     * 释放指定键对应的临时存储。
     *
     * @param handlerName 处理器名称。
     * @param key         指定的键。
     * @throws ServiceException 服务异常。
     */
    void dispose(@Nullable String handlerName, @Nonnull String key) throws ServiceException;

    /**
     * 移除指定键对应的临时存储。
     *
     * @param handlerName 处理器名称。
     * @param key         指定的键。
     * @throws ServiceException 服务异常。
     */
    void remove(@Nullable String handlerName, @Nonnull String key) throws ServiceException;

    /**
     * 如果指定的键对应的临时存储已释放，则移除该临时存储。
     *
     * @param handlerName 处理器名称。
     * @param key         指定的键。
     * @return 如果移除成功，则返回 <code>true</code>，否则返回 <code>false</code>。
     * @throws ServiceException 服务异常。
     */
    boolean removeIfDisposed(@Nullable String handlerName, @Nonnull String key) throws ServiceException;

    /**
     * 释放并移除指定键对应的临时存储。
     *
     * @param handlerName 处理器名称。
     * @param key         指定的键。
     * @throws ServiceException 服务异常。
     */
    void disposeAndRemove(@Nullable String handlerName, @Nonnull String key) throws ServiceException;

    /**
     * 清除处理器中已释放的临时存储。
     *
     * @param handlerName 处理器名称。
     * @throws ServiceException 服务异常。
     */
    void clearDisposed(@Nullable String handlerName) throws ServiceException;
}
