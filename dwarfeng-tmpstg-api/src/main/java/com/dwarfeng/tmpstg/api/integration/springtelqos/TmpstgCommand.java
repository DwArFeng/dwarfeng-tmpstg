package com.dwarfeng.tmpstg.api.integration.springtelqos;

import com.dwarfeng.dutil.basic.io.IOUtil;
import com.dwarfeng.springtelqos.sdk.command.CliCommand;
import com.dwarfeng.springtelqos.sdk.configuration.TelqosCommand;
import com.dwarfeng.springtelqos.sdk.util.CliCommandUtil;
import com.dwarfeng.springtelqos.stack.command.CommandDescriptor;
import com.dwarfeng.springtelqos.stack.command.CommandExecutor;
import com.dwarfeng.tmpstg.stack.bean.dto.TemporaryStorageInfo;
import com.dwarfeng.tmpstg.stack.service.TemporaryStorageQosService;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 临时存储指令。
 *
 * @author DwArFeng
 * @since 2.0.0
 */
@TelqosCommand
public class TmpstgCommand extends CliCommand {

    @SuppressWarnings({"SpellCheckingInspection", "GrazieInspectionRunner", "RedundantSuppression"})
    private static final String IDENTITY = "tmpstg";

    // region 指令选项

    private static final String COMMAND_OPTION_LIST_HANDLERS = "lh";
    private static final String COMMAND_OPTION_LIST_HANDLERS_LONG_OPT = "list-handlers";
    private static final String COMMAND_OPTION_IS_STARTED = "is";
    private static final String COMMAND_OPTION_IS_STARTED_LONG_OPT = "is-started";
    private static final String COMMAND_OPTION_START = "start";
    private static final String COMMAND_OPTION_STOP = "stop";
    private static final String COMMAND_OPTION_KEYS = "keys";
    private static final String COMMAND_OPTION_EXISTS = "exists";
    private static final String COMMAND_OPTION_INSPECT = "inspect";
    private static final String COMMAND_OPTION_CREATE = "create";
    private static final String COMMAND_OPTION_DISPOSE = "dispose";
    private static final String COMMAND_OPTION_REMOVE = "remove";
    private static final String COMMAND_OPTION_REMOVE_IF_DISPOSED = "rid";
    private static final String COMMAND_OPTION_REMOVE_IF_DISPOSED_LONG_OPT = "remove-if-disposed";
    private static final String COMMAND_OPTION_DISPOSE_AND_REMOVE = "dar";
    private static final String COMMAND_OPTION_DISPOSE_AND_REMOVE_LONG_OPT = "dispose-and-remove";
    private static final String COMMAND_OPTION_CLEAR_DISPOSED = "cd";
    private static final String COMMAND_OPTION_CLEAR_DISPOSED_LONG_OPT = "clear-disposed";
    private static final String COMMAND_OPTION_DOWNLOAD = "download";
    private static final String COMMAND_OPTION_UPLOAD = "upload";

    private static final String[] COMMAND_OPTION_ARRAY = new String[]{
            COMMAND_OPTION_LIST_HANDLERS,
            COMMAND_OPTION_IS_STARTED,
            COMMAND_OPTION_START,
            COMMAND_OPTION_STOP,
            COMMAND_OPTION_KEYS,
            COMMAND_OPTION_EXISTS,
            COMMAND_OPTION_INSPECT,
            COMMAND_OPTION_CREATE,
            COMMAND_OPTION_DISPOSE,
            COMMAND_OPTION_REMOVE,
            COMMAND_OPTION_REMOVE_IF_DISPOSED,
            COMMAND_OPTION_DISPOSE_AND_REMOVE,
            COMMAND_OPTION_CLEAR_DISPOSED,
            COMMAND_OPTION_DOWNLOAD,
            COMMAND_OPTION_UPLOAD
    };

    private static final String COMMAND_SUB_OPTION_HANDLER_NAME = "hn";
    private static final String COMMAND_SUB_OPTION_HANDLER_NAME_LONG_OPT = "handler-name";
    private static final String COMMAND_SUB_OPTION_KEY = "key";
    private static final String COMMAND_SUB_OPTION_FILE_PATH = "fp";
    private static final String COMMAND_SUB_OPTION_FILE_PATH_LONG_OPT = "file-path";

    // endregion

    private final TemporaryStorageQosService temporaryStorageQosService;

    public TmpstgCommand(TemporaryStorageQosService temporaryStorageQosService) {
        super(IDENTITY);
        this.temporaryStorageQosService = temporaryStorageQosService;
    }

    @Override
    protected DescriptionProvider provideDescriptionProvider() {
        return ctx -> "临时存储服务";
    }

    @Override
    protected CliSyntaxProvider provideCliSyntaxProvider() {
        return this::cliSyntaxProvider;
    }

    private String cliSyntaxProvider(CommandDescriptor.Context context) throws Exception {
        final String[] patterns = new String[]{
                context.getRuntimeIdentity() + " " + CliCommandUtil.concatOptionPrefix(COMMAND_OPTION_LIST_HANDLERS),
                context.getRuntimeIdentity() + " " + CliCommandUtil.concatOptionPrefix(COMMAND_OPTION_IS_STARTED) +
                        " [" + CliCommandUtil.concatOptionPrefix(COMMAND_SUB_OPTION_HANDLER_NAME) + " handler-name]",
                context.getRuntimeIdentity() + " " + CliCommandUtil.concatOptionPrefix(COMMAND_OPTION_START) +
                        " [" + CliCommandUtil.concatOptionPrefix(COMMAND_SUB_OPTION_HANDLER_NAME) + " handler-name]",
                context.getRuntimeIdentity() + " " + CliCommandUtil.concatOptionPrefix(COMMAND_OPTION_STOP) +
                        " [" + CliCommandUtil.concatOptionPrefix(COMMAND_SUB_OPTION_HANDLER_NAME) + " handler-name]",
                context.getRuntimeIdentity() + " " + CliCommandUtil.concatOptionPrefix(COMMAND_OPTION_KEYS) +
                        " [" + CliCommandUtil.concatOptionPrefix(COMMAND_SUB_OPTION_HANDLER_NAME) + " handler-name]",
                context.getRuntimeIdentity() + " " + CliCommandUtil.concatOptionPrefix(COMMAND_OPTION_EXISTS) +
                        " [" + CliCommandUtil.concatOptionPrefix(COMMAND_SUB_OPTION_HANDLER_NAME) + " handler-name] [" +
                        CliCommandUtil.concatOptionPrefix(COMMAND_SUB_OPTION_KEY) + " key]",
                context.getRuntimeIdentity() + " " + CliCommandUtil.concatOptionPrefix(COMMAND_OPTION_INSPECT) +
                        " [" + CliCommandUtil.concatOptionPrefix(COMMAND_SUB_OPTION_HANDLER_NAME) + " handler-name] [" +
                        CliCommandUtil.concatOptionPrefix(COMMAND_SUB_OPTION_KEY) + " key]",
                context.getRuntimeIdentity() + " " + CliCommandUtil.concatOptionPrefix(COMMAND_OPTION_CREATE) +
                        " [" + CliCommandUtil.concatOptionPrefix(COMMAND_SUB_OPTION_HANDLER_NAME) + " handler-name]",
                context.getRuntimeIdentity() + " " + CliCommandUtil.concatOptionPrefix(COMMAND_OPTION_DISPOSE) +
                        " [" + CliCommandUtil.concatOptionPrefix(COMMAND_SUB_OPTION_HANDLER_NAME) + " handler-name] [" +
                        CliCommandUtil.concatOptionPrefix(COMMAND_SUB_OPTION_KEY) + " key]",
                context.getRuntimeIdentity() + " " + CliCommandUtil.concatOptionPrefix(COMMAND_OPTION_REMOVE) +
                        " [" + CliCommandUtil.concatOptionPrefix(COMMAND_SUB_OPTION_HANDLER_NAME) + " handler-name] [" +
                        CliCommandUtil.concatOptionPrefix(COMMAND_SUB_OPTION_KEY) + " key]",
                context.getRuntimeIdentity() + " " +
                        CliCommandUtil.concatOptionPrefix(COMMAND_OPTION_REMOVE_IF_DISPOSED) + " [" +
                        CliCommandUtil.concatOptionPrefix(COMMAND_SUB_OPTION_HANDLER_NAME) + " handler-name] [" +
                        CliCommandUtil.concatOptionPrefix(COMMAND_SUB_OPTION_KEY) + " key]",
                context.getRuntimeIdentity() + " " +
                        CliCommandUtil.concatOptionPrefix(COMMAND_OPTION_DISPOSE_AND_REMOVE) + " [" +
                        CliCommandUtil.concatOptionPrefix(COMMAND_SUB_OPTION_HANDLER_NAME) + " handler-name] [" +
                        CliCommandUtil.concatOptionPrefix(COMMAND_SUB_OPTION_KEY) + " key]",
                context.getRuntimeIdentity() + " " +
                        CliCommandUtil.concatOptionPrefix(COMMAND_OPTION_CLEAR_DISPOSED) + " [" +
                        CliCommandUtil.concatOptionPrefix(COMMAND_SUB_OPTION_HANDLER_NAME) + " handler-name]",
                context.getRuntimeIdentity() + " " + CliCommandUtil.concatOptionPrefix(COMMAND_OPTION_DOWNLOAD) +
                        " [" + CliCommandUtil.concatOptionPrefix(COMMAND_SUB_OPTION_HANDLER_NAME) + " handler-name] [" +
                        CliCommandUtil.concatOptionPrefix(COMMAND_SUB_OPTION_KEY) + " key] [" +
                        CliCommandUtil.concatOptionPrefix(COMMAND_SUB_OPTION_FILE_PATH) + " file-path]",
                context.getRuntimeIdentity() + " " + CliCommandUtil.concatOptionPrefix(COMMAND_OPTION_UPLOAD) +
                        " [" + CliCommandUtil.concatOptionPrefix(COMMAND_SUB_OPTION_HANDLER_NAME) + " handler-name] [" +
                        CliCommandUtil.concatOptionPrefix(COMMAND_SUB_OPTION_KEY) + " key] [" +
                        CliCommandUtil.concatOptionPrefix(COMMAND_SUB_OPTION_FILE_PATH) + " file-path]"
        };
        return CliCommandUtil.cliSyntax(patterns);
    }

    @Override
    protected List<Option> provideOptions() {
        List<Option> list = new ArrayList<>();
        list.add(
                Option.builder(COMMAND_OPTION_LIST_HANDLERS).longOpt(COMMAND_OPTION_LIST_HANDLERS_LONG_OPT)
                        .optionalArg(true).hasArg(false).desc("列出所有可用的临时存储处理器").build()
        );
        list.add(
                Option.builder(COMMAND_OPTION_IS_STARTED).longOpt(COMMAND_OPTION_IS_STARTED_LONG_OPT)
                        .optionalArg(true).hasArg(false).desc("查看处理器是否已启动").build()
        );
        list.add(Option.builder(COMMAND_OPTION_START).optionalArg(true).hasArg(false).desc("启动处理器").build());
        list.add(Option.builder(COMMAND_OPTION_STOP).optionalArg(true).hasArg(false).desc("停止处理器").build());
        list.add(Option.builder(COMMAND_OPTION_KEYS).optionalArg(true).hasArg(false).desc("列出临时存储键").build());
        list.add(Option.builder(COMMAND_OPTION_EXISTS).optionalArg(true).hasArg(false).desc("判断指定键是否存在").build());
        list.add(Option.builder(COMMAND_OPTION_INSPECT).optionalArg(true).hasArg(false).desc("查看临时存储信息").build());
        list.add(Option.builder(COMMAND_OPTION_CREATE).optionalArg(true).hasArg(false).desc("创建新的临时存储").build());
        list.add(Option.builder(COMMAND_OPTION_DISPOSE).optionalArg(true).hasArg(false).desc("释放临时存储").build());
        list.add(Option.builder(COMMAND_OPTION_REMOVE).optionalArg(true).hasArg(false).desc("移除临时存储").build());
        list.add(
                Option.builder(COMMAND_OPTION_REMOVE_IF_DISPOSED)
                        .longOpt(COMMAND_OPTION_REMOVE_IF_DISPOSED_LONG_OPT)
                        .optionalArg(true).hasArg(false).desc("按状态移除临时存储").build()
        );
        list.add(
                Option.builder(COMMAND_OPTION_DISPOSE_AND_REMOVE)
                        .longOpt(COMMAND_OPTION_DISPOSE_AND_REMOVE_LONG_OPT)
                        .optionalArg(true).hasArg(false).desc("释放并移除临时存储").build()
        );
        list.add(
                Option.builder(COMMAND_OPTION_CLEAR_DISPOSED)
                        .longOpt(COMMAND_OPTION_CLEAR_DISPOSED_LONG_OPT)
                        .optionalArg(true).hasArg(false).desc("清除已释放的临时存储").build()
        );
        list.add(Option.builder(COMMAND_OPTION_DOWNLOAD).optionalArg(true).hasArg(false).desc("下载临时存储内容").build());
        list.add(Option.builder(COMMAND_OPTION_UPLOAD).optionalArg(true).hasArg(false).desc("上传临时存储内容").build());
        list.add(
                Option.builder(COMMAND_SUB_OPTION_HANDLER_NAME).longOpt(COMMAND_SUB_OPTION_HANDLER_NAME_LONG_OPT)
                        .hasArg(true).type(String.class).desc("处理器名称").build()
        );
        list.add(Option.builder(COMMAND_SUB_OPTION_KEY).hasArg(true).type(String.class).desc("临时存储键").build());
        list.add(
                Option.builder(COMMAND_SUB_OPTION_FILE_PATH).longOpt(COMMAND_SUB_OPTION_FILE_PATH_LONG_OPT)
                        .hasArg(true).type(String.class).desc("文件路径").build()
        );
        return list;
    }

    @Override
    protected void executeWithCmd(CommandExecutor.Context context, CommandLine cmd) throws Exception {
        Pair<String, Integer> pair = CliCommandUtil.analyseCommand(cmd, COMMAND_OPTION_ARRAY);
        if (pair.getRight() != 1) {
            context.sendMessage(CliCommandUtil.optionMismatchMessage(COMMAND_OPTION_ARRAY));
            context.sendMessage(context.getCommandManual(context.getRuntimeIdentity()));
            return;
        }
        switch (pair.getLeft()) {
            case COMMAND_OPTION_LIST_HANDLERS:
                handleListHandlers(context, cmd);
                break;
            case COMMAND_OPTION_IS_STARTED:
                handleIsStarted(context, cmd);
                break;
            case COMMAND_OPTION_START:
                handleStart(context, cmd);
                break;
            case COMMAND_OPTION_STOP:
                handleStop(context, cmd);
                break;
            case COMMAND_OPTION_KEYS:
                handleKeys(context, cmd);
                break;
            case COMMAND_OPTION_EXISTS:
                handleExists(context, cmd);
                break;
            case COMMAND_OPTION_INSPECT:
                handleInspect(context, cmd);
                break;
            case COMMAND_OPTION_CREATE:
                handleCreate(context, cmd);
                break;
            case COMMAND_OPTION_DISPOSE:
                handleDispose(context, cmd);
                break;
            case COMMAND_OPTION_REMOVE:
                handleRemove(context, cmd);
                break;
            case COMMAND_OPTION_REMOVE_IF_DISPOSED:
                handleRemoveIfDisposed(context, cmd);
                break;
            case COMMAND_OPTION_DISPOSE_AND_REMOVE:
                handleDisposeAndRemove(context, cmd);
                break;
            case COMMAND_OPTION_CLEAR_DISPOSED:
                handleClearDisposed(context, cmd);
                break;
            case COMMAND_OPTION_DOWNLOAD:
                handleDownload(context, cmd);
                break;
            case COMMAND_OPTION_UPLOAD:
                handleUpload(context, cmd);
                break;
            default:
                throw new IllegalStateException("不应该执行到此处, 请联系开发人员");
        }
    }

    private void handleListHandlers(
            CommandExecutor.Context context,
            @SuppressWarnings("unused") CommandLine cmd
    ) throws Exception {
        // 调用服务，获取所有处理器的名称。
        List<String> handlerNames = temporaryStorageQosService.listHandlerNames();

        // 输出结果。
        context.sendMessage("可用的处理器名称: ");
        if (handlerNames.isEmpty()) {
            context.sendMessage("  (Empty)");
            return;
        }
        for (int i = 0; i < handlerNames.size(); i++) {
            context.sendMessage(String.format("  %3d: %s", i + 1, handlerNames.get(i)));
        }
    }

    private void handleIsStarted(CommandExecutor.Context context, CommandLine cmd) throws Exception {
        // 获取处理器名称。
        String handlerName = parseHandlerName(context, cmd);

        // 调用服务，获取处理器启动状态。
        boolean started = temporaryStorageQosService.isStarted(handlerName);

        // 输出结果。
        context.sendMessage("处理器名称: " + normalizeHandlerNameForOutput(handlerName) + ", 已启动: " + started);
    }

    private void handleStart(CommandExecutor.Context context, CommandLine cmd) throws Exception {
        // 获取处理器名称。
        String handlerName = parseHandlerName(context, cmd);

        // 调用服务，启动处理器。
        temporaryStorageQosService.start(handlerName);

        // 输出结果。
        context.sendMessage("启动成功!");
        context.sendMessage("处理器名称: " + normalizeHandlerNameForOutput(handlerName));
    }

    private void handleStop(CommandExecutor.Context context, CommandLine cmd) throws Exception {
        // 获取处理器名称。
        String handlerName = parseHandlerName(context, cmd);

        // 调用服务，停止处理器。
        temporaryStorageQosService.stop(handlerName);

        // 输出结果。
        context.sendMessage("停止成功!");
        context.sendMessage("处理器名称: " + normalizeHandlerNameForOutput(handlerName));
    }

    private void handleKeys(CommandExecutor.Context context, CommandLine cmd) throws Exception {
        // 获取处理器名称。
        String handlerName = parseHandlerName(context, cmd);

        // 调用服务，获取临时存储键集合。
        Collection<String> keys = temporaryStorageQosService.keys(handlerName);

        // 输出结果。
        context.sendMessage("处理器名称: " + normalizeHandlerNameForOutput(handlerName));
        context.sendMessage("可用的临时存储键: ");
        if (keys.isEmpty()) {
            context.sendMessage("  (Empty)");
            return;
        }
        int i = 1;
        for (String key : keys) {
            context.sendMessage(String.format("  %3d: %s", i++, key));
        }
    }

    private void handleExists(CommandExecutor.Context context, CommandLine cmd) throws Exception {
        // 获取处理器名称和临时存储键。
        String handlerName = parseHandlerName(context, cmd);
        String key = parseKey(context, cmd);

        // 调用服务，判断临时存储键是否存在。
        boolean exists = temporaryStorageQosService.exists(handlerName, key);

        // 输出结果。
        context.sendMessage(
                "处理器名称: " + normalizeHandlerNameForOutput(handlerName) + ", 临时存储键: " + key + ", 存在: " + exists
        );
    }

    private void handleInspect(CommandExecutor.Context context, CommandLine cmd) throws Exception {
        // 获取处理器名称和临时存储键。
        String handlerName = parseHandlerName(context, cmd);
        String key = parseKey(context, cmd);

        // 调用服务，查看临时存储信息。
        TemporaryStorageInfo info = temporaryStorageQosService.inspect(handlerName, key);

        // 输出结果。
        context.sendMessage("临时存储信息: ");
        context.sendMessage("  key: " + info.getKey());
        context.sendMessage("  memoryBufferAllocatedLength: " + info.getMemoryBufferAllocatedLength());
        context.sendMessage("  memoryBufferActualLength: " + info.getMemoryBufferActualLength());
        context.sendMessage("  fileBufferUsed: " + info.isFileBufferUsed());
        context.sendMessage("  fileBufferActualLength: " + info.getFileBufferActualLength());
        context.sendMessage("  status: " + info.getStatus());
        context.sendMessage("  contentLength: " + info.getContentLength());
    }

    private void handleCreate(CommandExecutor.Context context, CommandLine cmd) throws Exception {
        // 获取处理器名称。
        String handlerName = parseHandlerName(context, cmd);

        // 调用服务，创建临时存储。
        String key = temporaryStorageQosService.create(handlerName);

        // 输出结果。
        context.sendMessage("创建成功!");
        context.sendMessage("处理器名称: " + normalizeHandlerNameForOutput(handlerName) + ", 临时存储键: " + key);
    }

    private void handleDispose(CommandExecutor.Context context, CommandLine cmd) throws Exception {
        // 获取处理器名称和临时存储键。
        String handlerName = parseHandlerName(context, cmd);
        String key = parseKey(context, cmd);

        // 调用服务，释放临时存储。
        temporaryStorageQosService.dispose(handlerName, key);

        // 输出结果。
        context.sendMessage("释放成功!");
        context.sendMessage("处理器名称: " + normalizeHandlerNameForOutput(handlerName) + ", 临时存储键: " + key);
    }

    private void handleRemove(CommandExecutor.Context context, CommandLine cmd) throws Exception {
        // 获取处理器名称和临时存储键。
        String handlerName = parseHandlerName(context, cmd);
        String key = parseKey(context, cmd);

        // 调用服务，移除临时存储。
        temporaryStorageQosService.remove(handlerName, key);

        // 输出结果。
        context.sendMessage("移除成功!");
        context.sendMessage("处理器名称: " + normalizeHandlerNameForOutput(handlerName) + ", 临时存储键: " + key);
    }

    private void handleRemoveIfDisposed(CommandExecutor.Context context, CommandLine cmd) throws Exception {
        // 获取处理器名称和临时存储键。
        String handlerName = parseHandlerName(context, cmd);
        String key = parseKey(context, cmd);

        // 调用服务，按状态移除临时存储。
        boolean removed = temporaryStorageQosService.removeIfDisposed(handlerName, key);

        // 输出结果。
        context.sendMessage(
                "处理器名称: " + normalizeHandlerNameForOutput(handlerName) + ", 临时存储键: " + key + ", 已移除: " + removed
        );
    }

    private void handleDisposeAndRemove(CommandExecutor.Context context, CommandLine cmd) throws Exception {
        // 获取处理器名称和临时存储键。
        String handlerName = parseHandlerName(context, cmd);
        String key = parseKey(context, cmd);

        // 调用服务，释放并移除临时存储。
        temporaryStorageQosService.disposeAndRemove(handlerName, key);

        // 输出结果。
        context.sendMessage("释放并移除成功!");
        context.sendMessage("处理器名称: " + normalizeHandlerNameForOutput(handlerName) + ", 临时存储键: " + key);
    }

    private void handleClearDisposed(CommandExecutor.Context context, CommandLine cmd) throws Exception {
        // 获取处理器名称。
        String handlerName = parseHandlerName(context, cmd);

        // 调用服务，清除已释放临时存储。
        temporaryStorageQosService.clearDisposed(handlerName);

        // 输出结果。
        context.sendMessage("清理成功!");
        context.sendMessage("处理器名称: " + normalizeHandlerNameForOutput(handlerName));
    }

    private void handleDownload(CommandExecutor.Context context, CommandLine cmd) throws Exception {
        // 获取处理器名称、临时存储键与文件路径。
        String handlerName = parseHandlerName(context, cmd);
        String key = parseKey(context, cmd);
        String filePath = parseFilePath(context, cmd);

        // 下载文件。
        try (
                InputStream in = temporaryStorageQosService.openInputStream(handlerName, key);
                OutputStream out = Files.newOutputStream(
                        new File(filePath).toPath(),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.TRUNCATE_EXISTING
                )
        ) {
            IOUtil.trans(in, out, 4096);
        }

        // 输出结果。
        context.sendMessage("下载成功!");
        context.sendMessage(
                "处理器名称: " + normalizeHandlerNameForOutput(handlerName) + ", 临时存储键: " + key + ", 文件路径: " + filePath
        );
    }

    private void handleUpload(CommandExecutor.Context context, CommandLine cmd) throws Exception {
        // 获取处理器名称、临时存储键与文件路径。
        String handlerName = parseHandlerName(context, cmd);
        String key = parseKey(context, cmd);
        String filePath = parseFilePath(context, cmd);

        // 检查本地文件有效性。
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            context.sendMessage("上传失败: 指定路径不是有效文件: " + filePath);
            return;
        }

        // 上传文件。
        try (
                InputStream in = Files.newInputStream(file.toPath());
                OutputStream out = temporaryStorageQosService.openOutputStream(handlerName, key, file.length())
        ) {
            IOUtil.trans(in, out, 4096);
        }

        // 输出结果。
        context.sendMessage("上传成功!");
        context.sendMessage(
                "处理器名称: " + normalizeHandlerNameForOutput(handlerName) + ", 临时存储键: " + key + ", 文件路径: " + filePath
        );
    }

    private String parseHandlerName(CommandExecutor.Context context, CommandLine cmd) throws Exception {
        // 如果有 COMMAND_SUB_OPTION_HANDLER_NAME 选项，则直接获取 handlerName。
        if (cmd.hasOption(COMMAND_SUB_OPTION_HANDLER_NAME)) {
            return StringUtils.trimToNull(cmd.getOptionValue(COMMAND_SUB_OPTION_HANDLER_NAME));
        }

        // 如果没有 COMMAND_SUB_OPTION_HANDLER_NAME 选项，则根据处理器数量决定行为。
        List<String> handlerNames = temporaryStorageQosService.listHandlerNames();
        if (handlerNames.size() <= 1) {
            return null;
        }

        // 多处理器场景下，先输出处理器列表，再交互式输入。
        context.sendMessage("可用的处理器名称: ");
        for (int i = 0; i < handlerNames.size(); i++) {
            context.sendMessage(String.format("  %3d: %s", i + 1, handlerNames.get(i)));
        }
        context.sendMessage("请输入处理器名称:");
        return StringUtils.trimToNull(context.receiveMessage());
    }

    private String parseKey(CommandExecutor.Context context, CommandLine cmd) throws Exception {
        if (cmd.hasOption(COMMAND_SUB_OPTION_KEY)) {
            String key = StringUtils.trim((String) cmd.getParsedOptionValue(COMMAND_SUB_OPTION_KEY));
            if (StringUtils.isNotEmpty(key)) {
                return key;
            }
        }
        context.sendMessage("请输入临时存储键:");
        return context.receiveMessage();
    }

    private String parseFilePath(CommandExecutor.Context context, CommandLine cmd) throws Exception {
        if (cmd.hasOption(COMMAND_SUB_OPTION_FILE_PATH)) {
            String filePath = StringUtils.trim((String) cmd.getParsedOptionValue(COMMAND_SUB_OPTION_FILE_PATH));
            if (StringUtils.isNotEmpty(filePath)) {
                return filePath;
            }
        }
        context.sendMessage("请输入文件路径:");
        return context.receiveMessage();
    }

    private String normalizeHandlerNameForOutput(@Nullable String handlerName) {
        return StringUtils.defaultIfBlank(handlerName, "<default>");
    }
}
