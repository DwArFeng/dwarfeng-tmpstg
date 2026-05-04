package com.dwarfeng.tmpstg.node.configuration;

import com.dwarfeng.tmpstg.impl.handler.TemporaryStorageHandlerImpl;
import com.dwarfeng.tmpstg.sdk.util.BeanDefinitionParserUtil;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;

/**
 * Tmpstg Handler 元素的 BeanDefinitionParser。
 *
 * @author DwArFeng
 * @since 2.0.0
 */
public class TemporaryStorageHandlerDefinitionParser implements BeanDefinitionParser {

    @Override
    public BeanDefinition parse(Element element, @Nonnull ParserContext parserContext) {
        String handlerName = (String) BeanDefinitionParserUtil.mayResolveSpel(
                parserContext, element.getAttribute("handler-name")
        );
        String configRef = (String) BeanDefinitionParserUtil.mayResolveSpel(
                parserContext, element.getAttribute("config-ref")
        );
        String schedulerRef = (String) BeanDefinitionParserUtil.mayResolveSpel(
                parserContext, element.getAttribute("scheduler-ref")
        );
        String autoStart = (String) BeanDefinitionParserUtil.mayResolveSpel(
                parserContext, element.getAttribute("auto-start")
        );

        BeanDefinitionParserUtil.makeSureBeanNameNotDuplicated(parserContext, handlerName);

        BeanDefinitionBuilder temporaryStorageHandlerBuilder = BeanDefinitionBuilder.rootBeanDefinition(
                TemporaryStorageHandlerImpl.class
        );
        temporaryStorageHandlerBuilder.getRawBeanDefinition().setAutowireMode(
                AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR
        );
        ConstructorArgumentValues constructorArgumentValues = new ConstructorArgumentValues();
        constructorArgumentValues.addIndexedArgumentValue(0, new RuntimeBeanReference(schedulerRef));
        constructorArgumentValues.addIndexedArgumentValue(1, new RuntimeBeanReference(configRef));
        temporaryStorageHandlerBuilder.getRawBeanDefinition().setConstructorArgumentValues(constructorArgumentValues);
        if (Boolean.parseBoolean(autoStart)) {
            temporaryStorageHandlerBuilder.setInitMethodName("start");
        }
        temporaryStorageHandlerBuilder.setScope(BeanDefinition.SCOPE_SINGLETON);
        temporaryStorageHandlerBuilder.setLazyInit(false);
        parserContext.getRegistry().registerBeanDefinition(
                handlerName, temporaryStorageHandlerBuilder.getBeanDefinition()
        );

        return null;
    }
}
