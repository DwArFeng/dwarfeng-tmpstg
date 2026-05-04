package com.dwarfeng.tmpstg.node.configuration;

import com.dwarfeng.tmpstg.impl.handler.TemporaryStorageQosHandlerImpl;
import com.dwarfeng.tmpstg.impl.service.TemporaryStorageQosServiceImpl;
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
 * Tmpstg Qos 元素的 BeanDefinitionParser。
 *
 * @author DwArFeng
 * @since 2.0.0
 */
public class TemporaryStorageQosDefinitionParser implements BeanDefinitionParser {

    @Override
    public BeanDefinition parse(Element element, @Nonnull ParserContext parserContext) {
        String qosHandlerName = (String) BeanDefinitionParserUtil.mayResolveSpel(
                parserContext, element.getAttribute("qos-handler-name")
        );
        String qosServiceName = (String) BeanDefinitionParserUtil.mayResolveSpel(
                parserContext, element.getAttribute("qos-service-name")
        );
        String semRef = (String) BeanDefinitionParserUtil.mayResolveSpel(
                parserContext, element.getAttribute("sem-ref")
        );

        BeanDefinitionParserUtil.makeSureBeanNameNotDuplicated(parserContext, qosHandlerName);
        BeanDefinitionParserUtil.makeSureBeanNameNotDuplicated(parserContext, qosServiceName);

        BeanDefinitionBuilder temporaryStorageQosHandlerBuilder = BeanDefinitionBuilder.rootBeanDefinition(
                TemporaryStorageQosHandlerImpl.class
        );
        temporaryStorageQosHandlerBuilder.getRawBeanDefinition().setAutowireMode(
                AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR
        );
        temporaryStorageQosHandlerBuilder.setScope(BeanDefinition.SCOPE_SINGLETON);
        temporaryStorageQosHandlerBuilder.setLazyInit(false);
        parserContext.getRegistry().registerBeanDefinition(
                qosHandlerName, temporaryStorageQosHandlerBuilder.getBeanDefinition()
        );

        BeanDefinitionBuilder temporaryStorageQosServiceBuilder = BeanDefinitionBuilder.rootBeanDefinition(
                TemporaryStorageQosServiceImpl.class
        );
        temporaryStorageQosServiceBuilder.getRawBeanDefinition().setAutowireMode(
                AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR
        );
        ConstructorArgumentValues temporaryStorageQosServiceConstructorArgumentValues = new ConstructorArgumentValues();
        temporaryStorageQosServiceConstructorArgumentValues.addIndexedArgumentValue(
                0, new RuntimeBeanReference(qosHandlerName)
        );
        temporaryStorageQosServiceConstructorArgumentValues.addIndexedArgumentValue(
                1, new RuntimeBeanReference(semRef)
        );
        temporaryStorageQosServiceBuilder.getRawBeanDefinition().setConstructorArgumentValues(
                temporaryStorageQosServiceConstructorArgumentValues
        );
        temporaryStorageQosServiceBuilder.setScope(BeanDefinition.SCOPE_SINGLETON);
        temporaryStorageQosServiceBuilder.setLazyInit(false);
        parserContext.getRegistry().registerBeanDefinition(
                qosServiceName, temporaryStorageQosServiceBuilder.getBeanDefinition()
        );

        return null;
    }
}
