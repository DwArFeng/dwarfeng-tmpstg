package com.dwarfeng.tmpstg.core.internal.i18n;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * {@link CoreMessages} 的消息资源测试。
 */
class CoreMessagesTest {

    @Test
    void shouldContainEnglishAndChineseMessagesForEveryKey() {
        for (CoreMessageKey key : CoreMessageKey.values()) {
            String english = CoreMessages.message(Locale.ENGLISH, key);
            String chinese = CoreMessages.message(Locale.SIMPLIFIED_CHINESE, key);

            assertFalse(english.isBlank());
            assertFalse(chinese.isBlank());
            assertNotEquals(english, chinese);
        }
    }
}
