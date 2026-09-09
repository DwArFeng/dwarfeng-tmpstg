package com.dwarfeng.tmpstg.core.internal.i18n;

import com.dwarfeng.tmpstg.base.sdk.i18n.Messages;
import com.dwarfeng.tmpstg.base.stack.i18n.MessageCatalog;

import java.util.Locale;

/**
 * Core 模块消息入口。
 *
 * @author DwArFeng
 * @since 3.0.0
 */
public final class CoreMessages {

    private CoreMessages() {
        throw new AssertionError("No instances");
    }

    /**
     * 使用当前语言环境解析消息。
     *
     * @param key  消息键。
     * @param args 格式化参数。
     * @return 解析后的消息。
     */
    public static String message(CoreMessageKey key, Object... args) {
        return Messages.resolve(key.catalog().messageCatalog(), key.key(), args);
    }

    /**
     * 使用指定语言环境解析消息。
     *
     * @param locale 语言环境。
     * @param key    消息键。
     * @param args   格式化参数。
     * @return 解析后的消息。
     */
    public static String message(Locale locale, CoreMessageKey key, Object... args) {
        return Messages.resolve(key.catalog().messageCatalog(), key.key(), locale, args);
    }

    /**
     * Core 模块消息目录。
     */
    enum Catalog {

        SDK("com.dwarfeng.tmpstg.sdk.i18n.messages");

        private final MessageCatalog messageCatalog;

        Catalog(String baseName) {
            this.messageCatalog = MessageCatalog.of(CoreMessages.class, baseName);
        }

        MessageCatalog messageCatalog() {
            return messageCatalog;
        }
    }
}
