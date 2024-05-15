package com.dwarfeng.tmpstg.handler;

import com.dwarfeng.subgrade.stack.exception.HandlerException;
import com.dwarfeng.subgrade.stack.handler.StartableHandler;
import com.dwarfeng.tmpstg.bean.dto.TemporaryStorageInfo;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

/**
 * 临时存储处理器。
 *
 * <p>
 * 该接口的实现应该是线程安全的。
 *
 * @author DwArFeng
 * @since 1.0.0
 */
public interface TemporaryStorageHandler extends StartableHandler {

    /**
     * 获取临时存储处理器维护的临时存储的键集合。
     *
     * <p>
     * 该方法必须在处理器启动后调用，否则将抛出异常。
     *
     * <p>
     * 该方法返回的集合是只读的。
     *
     * @return 临时存储处理器维护的临时存储的键集合。
     * @throws HandlerException 处理器异常。
     */
    Collection<String> keys() throws HandlerException;

    /**
     * 判断指定的键是否存在。
     *
     * <p>
     * 该方法必须在处理器启动后调用，否则将抛出异常。
     *
     * @param key 指定的键。
     * @return 指定的键是否存在。
     * @throws HandlerException 处理器异常。
     */
    boolean exists(String key) throws HandlerException;

    /**
     * 获取指定键对应的临时存储信息。
     *
     * <p>
     * 该方法必须在处理器启动后调用，否则将抛出异常。
     *
     * <p>
     * 指定的键必须存在，否则将抛出异常。
     *
     * @param key 指定的键。
     * @return 指定键对应的临时存储信息。
     * @throws HandlerException 处理器异常。
     */
    TemporaryStorageInfo inspect(String key) throws HandlerException;

    /**
     * 创建一个新的临时存储。
     *
     * <p>
     * 该方法必须在处理器启动后调用，否则将抛出异常。
     *
     * @return 新创建的临时存储的键。
     * @throws HandlerException 处理器异常。
     */
    String create() throws HandlerException;

    /**
     * 打开指定键对应的输入流。
     *
     * <p>
     * 该方法必须在处理器启动后调用，否则将抛出异常。
     *
     * <p>
     * 指定的键必须存在，否则将抛出异常。
     *
     * <p>
     * 指定的键对应的临时存储的状态必须是工作中，否则将抛出异常。
     *
     * <p>
     * 该方法不会关闭流，需要调用者自行关闭，请 <b>务必</b> 在调用该方法结束后关闭流，否则会造成处理器后续操作阻塞等行为异常。
     *
     * @param key 指定的键。
     * @return 指定键对应的输入流。
     * @throws HandlerException 处理器异常。
     */
    InputStream openInputStream(String key) throws HandlerException;

    /**
     * 打开指定键对应的输出流。
     *
     * <p>
     * 生成的输出流将会调度其内部的内存缓冲和临时文件缓冲，以处理写入的数据。<br>
     * 输出流处理据写入时，会首先写入内存缓冲，当内存缓冲写满时，输出流会尝试申请更大的内存缓冲。<br>
     * 当内存缓冲的大小达到上限或其它原因无法申请更大的内存缓冲时，输出流会创建一个临时文件，并初始化文件缓冲，
     * 将剩余的数据写入文件缓冲。<br>
     * 当输出流关闭时，如果内存缓没有被写满，那么多余的内存缓冲空间将会被释放，归还给处理器，以供其它输出流使用。
     *
     * <p>
     * 该方法将会申请一个当前状态下能够申请的最大长度的内存缓冲。<br>
     * 对于长度未知的数据写入，推荐使用该方法，以避免频繁的申请内存缓冲。<br>
     * 如果数据长度已知，推荐使用 {@link #openOutputStream(String, long)} 方法，以避免不必要的内存缓冲申请和释放。
     *
     * <p>
     * 该方法必须在处理器启动后调用，否则将抛出异常。
     *
     * <p>
     * 指定的键必须存在，否则将抛出异常。
     *
     * <p>
     * 指定的键对应的临时存储的状态必须是工作中，否则将抛出异常。
     *
     * <p>
     * 该方法不会关闭流，需要调用者自行关闭，请 <b>务必</b> 在调用该方法结束后关闭流，否则会造成处理器后续操作阻塞等行为异常。
     *
     * @param key 指定的键。
     * @return 指定键对应的输出流。
     * @throws HandlerException 处理器异常。
     * @see #openOutputStream(String, long)
     */
    OutputStream openOutputStream(String key) throws HandlerException;

    /**
     * 打开指定键对应的输出流。
     *
     * <p>
     * 生成的输出流将会调度其内部的内存缓冲和临时文件缓冲，以处理写入的数据。<br>
     * 输出流处理据写入时，会首先写入内存缓冲，当内存缓冲写满时，输出流会尝试申请更大的内存缓冲。<br>
     * 当内存缓冲的大小达到上限或其它原因无法申请更大的内存缓冲时，输出流会创建一个临时文件，并初始化文件缓冲，
     * 将剩余的数据写入文件缓冲。<br>
     * 当输出流关闭时，如果内存缓没有被写满，那么多余的内存缓冲空间将会被释放，归还给处理器，以供其它输出流使用。
     *
     * <p>
     * 该方法将会申请一个适配 <code>expectedLength</code> 的内存缓冲。<br>
     * 对于长度未知的数据写入，推荐使用 {@link #openOutputStream(String)} 方法，以避免频繁的申请内存缓冲。<br>
     * 如果数据长度已知，推荐使用该方法，以避免不必要的内存缓冲申请和释放。
     *
     * <p>
     * 该方法必须在处理器启动后调用，否则将抛出异常。
     *
     * <p>
     * 指定的键必须存在，否则将抛出异常。
     *
     * <p>
     * 指定的键对应的临时存储的状态必须是工作中，否则将抛出异常。
     *
     * <p>
     * 该方法不会关闭流，需要调用者自行关闭，请 <b>务必</b> 在调用该方法结束后关闭流，否则会造成处理器后续操作阻塞等行为异常。
     *
     * @param key            指定的键。
     * @param expectedLength 期望的数据长度。
     * @return 指定键对应的输出流。
     * @throws HandlerException 处理器异常。
     * @see #openOutputStream(String)
     */
    OutputStream openOutputStream(String key, long expectedLength) throws HandlerException;

    /**
     * 释放指定键对应的临时存储。
     *
     * <p>
     * 该方法会删除指定键对应的临时存储使用的临时文件（如果存在），释放内存缓冲，并将临时存储的状态设置为已释放。<br>
     * 释放后的临时存储将不再可用，且状态不可恢复为工作中。<br>
     * 该方法不会从处理器中移除指定键对应的临时存储，如果需要移除，请使用 {@link #remove(String)} 方法，
     * 或是等待处理器定时清理（需要启动处理器的定时清理功能）。<br>
     * 在释放后到移除之前，仍任可以通过 {@link #inspect(String)} 方法查看指定键对应的临时存储的信息。
     *
     * <p>
     * 该方法必须在处理器启动后调用，否则将抛出异常。
     *
     * <p>
     * 指定的键必须存在，否则将抛出异常。
     *
     * <p>
     * 指定的键对应的临时存储的状态必须是工作中，否则将抛出异常。
     *
     * @param key 指定的键。
     * @throws HandlerException 处理器异常。
     */
    void dispose(String key) throws HandlerException;

    /**
     * 移除指定键对应的临时存储。
     *
     * <p>
     * 该方法必须在处理器启动后调用，否则将抛出异常。
     *
     * <p>
     * 指定的键必须存在，否则将抛出异常。
     *
     * <p>
     * 指定的键对应的临时存储的状态必须是已释放，否则将抛出异常。
     *
     * @param key 指定的键。
     * @throws HandlerException 处理器异常。
     */
    void remove(String key) throws HandlerException;

    /**
     * 如果指定的键对应的临时存储已释放，则移除该临时存储。
     *
     * <p>
     * 该方法必须在处理器启动后调用，否则将抛出异常。
     *
     * <p>
     * 指定的键必须存在，否则将抛出异常。
     *
     * @param key 指定的键。
     * @return 如果移除成功，则返回 <code>true</code>，否则返回 <code>false</code>。
     * @throws HandlerException 处理器异常。
     */
    boolean removeIfDisposed(String key) throws HandlerException;

    /**
     * 释放并移除指定键对应的临时存储。
     *
     * <p>
     * 该方法等效于连续调用 {@link #dispose(String)} 和 {@link #remove(String)} 方法。
     *
     * <p>
     * 该方法必须在处理器启动后调用，否则将抛出异常。
     *
     * <p>
     * 指定的键必须存在，否则将抛出异常。
     *
     * <p>
     * 指定的键对应的临时存储的状态必须是工作中，否则将抛出异常。
     *
     * @param key 指定的键。
     * @throws HandlerException 处理器异常。
     */
    void disposeAndRemove(String key) throws HandlerException;

    /**
     * 清除处理器中已释放的临时存储。
     *
     * <p>
     * 该方法必须在处理器启动后调用，否则将抛出异常。
     *
     * @throws HandlerException 处理器异常。
     */
    void clearDisposed() throws HandlerException;
}
