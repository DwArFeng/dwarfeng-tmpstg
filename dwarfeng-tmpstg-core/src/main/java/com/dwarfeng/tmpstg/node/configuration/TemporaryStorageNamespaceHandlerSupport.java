package com.dwarfeng.tmpstg.node.configuration;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Tmpstg 命名空间处理器。
 *
 * @author DwArFeng
 * @since 2.0.0
 */
public class TemporaryStorageNamespaceHandlerSupport extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("config", new TemporaryStorageConfigDefinitionParser());
        registerBeanDefinitionParser("handler", new TemporaryStorageHandlerDefinitionParser());
        registerBeanDefinitionParser("qos", new TemporaryStorageQosDefinitionParser());
    }
}
