module com.dwarfeng.tmpstg.core {

    requires com.dwarfeng.tmpstg.base;
    requires com.dwarfeng.dutil.basic;
    requires com.dwarfeng.subgrade.aop;
    requires com.dwarfeng.subgrade.basic;
    requires com.dwarfeng.subgrade.lifecycle;
    requires jakarta.annotation;
    requires org.apache.commons.lang3;
    requires org.slf4j;
    requires spring.beans;
    requires spring.context;
    requires spring.expression;
    requires static org.jetbrains.annotations;
    requires java.xml;

    exports com.dwarfeng.tmpstg.impl.handler;
    exports com.dwarfeng.tmpstg.impl.service;
    exports com.dwarfeng.tmpstg.node.configuration;
    exports com.dwarfeng.tmpstg.sdk.exception;
    exports com.dwarfeng.tmpstg.sdk.util;
    exports com.dwarfeng.tmpstg.stack.bean.dto;
    exports com.dwarfeng.tmpstg.stack.exception;
    exports com.dwarfeng.tmpstg.stack.handler;
    exports com.dwarfeng.tmpstg.stack.service;
    exports com.dwarfeng.tmpstg.stack.struct;
    exports com.dwarfeng.tmpstg.stack.util;

    opens com.dwarfeng.tmpstg.sdk.i18n to com.dwarfeng.tmpstg.base;
}
