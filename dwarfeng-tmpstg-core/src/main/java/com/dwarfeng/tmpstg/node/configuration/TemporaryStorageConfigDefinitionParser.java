package com.dwarfeng.tmpstg.node.configuration;

import com.dwarfeng.tmpstg.sdk.util.BeanDefinitionParserUtil;
import com.dwarfeng.tmpstg.stack.struct.TemporaryStorageConfig;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;

/**
 * Tmpstg Config 元素的 BeanDefinitionParser。
 *
 * @author DwArFeng
 * @since 2.0.0
 */
public class TemporaryStorageConfigDefinitionParser implements BeanDefinitionParser {

    @Override
    public BeanDefinition parse(Element element, @Nonnull ParserContext parserContext) {
        String configName = (String) BeanDefinitionParserUtil.mayResolveSpel(
                parserContext, element.getAttribute("config-name")
        );

        BeanDefinitionParserUtil.makeSureBeanNameNotDuplicated(parserContext, configName);

        RootBeanDefinition temporaryStorageConfigBuilderBeanDefinition =
                new RootBeanDefinition(TemporaryStorageConfig.Builder.class);
        temporaryStorageConfigBuilderBeanDefinition.getPropertyValues().add(
                "temporaryFileDirectoryPath",
                BeanDefinitionParserUtil.mayResolvePlaceholder(
                        parserContext, element.getAttribute("temporary-file-directory-path")
                )
        );
        temporaryStorageConfigBuilderBeanDefinition.getPropertyValues().add(
                "temporaryFilePrefix",
                BeanDefinitionParserUtil.mayResolvePlaceholder(
                        parserContext, element.getAttribute("temporary-file-prefix")
                )
        );
        temporaryStorageConfigBuilderBeanDefinition.getPropertyValues().add(
                "temporaryFileSuffix",
                BeanDefinitionParserUtil.mayResolvePlaceholder(
                        parserContext, element.getAttribute("temporary-file-suffix")
                )
        );
        temporaryStorageConfigBuilderBeanDefinition.getPropertyValues().add(
                "maxBufferSizePerStorage",
                BeanDefinitionParserUtil.mayResolvePlaceholder(
                        parserContext, element.getAttribute("max-buffer-size-per-storage")
                )
        );
        temporaryStorageConfigBuilderBeanDefinition.getPropertyValues().add(
                "maxBufferSizeTotal",
                BeanDefinitionParserUtil.mayResolvePlaceholder(
                        parserContext, element.getAttribute("max-buffer-size-total")
                )
        );
        temporaryStorageConfigBuilderBeanDefinition.getPropertyValues().add(
                "clearDisposedInterval",
                BeanDefinitionParserUtil.mayResolvePlaceholder(
                        parserContext, element.getAttribute("clear-disposed-interval")
                )
        );
        temporaryStorageConfigBuilderBeanDefinition.getPropertyValues().add(
                "checkMemoryInterval",
                BeanDefinitionParserUtil.mayResolvePlaceholder(
                        parserContext, element.getAttribute("check-memory-interval")
                )
        );
        temporaryStorageConfigBuilderBeanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
        temporaryStorageConfigBuilderBeanDefinition.setLazyInit(false);
        String temporaryStorageConfigBuilderBeanName = BeanDefinitionParserUtil.parseAvailableBeanName(
                parserContext, configName + "Builder"
        );
        parserContext.getRegistry().registerBeanDefinition(
                temporaryStorageConfigBuilderBeanName, temporaryStorageConfigBuilderBeanDefinition
        );

        RootBeanDefinition temporaryStorageConfigBeanDefinition = new RootBeanDefinition(TemporaryStorageConfig.class);
        temporaryStorageConfigBeanDefinition.setFactoryBeanName(temporaryStorageConfigBuilderBeanName);
        temporaryStorageConfigBeanDefinition.setFactoryMethodName("build");
        temporaryStorageConfigBeanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
        temporaryStorageConfigBeanDefinition.setLazyInit(false);
        parserContext.getRegistry().registerBeanDefinition(configName, temporaryStorageConfigBeanDefinition);

        return null;
    }
}
