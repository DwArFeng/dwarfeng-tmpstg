package com.dwarfeng.tmpstg.node.configuration;

import com.dwarfeng.subgrade.basic.impl.exception.MapServiceExceptionMapper;
import com.dwarfeng.subgrade.basic.sdk.exception.ServiceExceptionHelper;
import com.dwarfeng.subgrade.basic.stack.exception.ServiceException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.function.Supplier;

@Configuration
public class ServiceExceptionMapperConfiguration {

    @Bean
    public MapServiceExceptionMapper mapServiceExceptionMapper() {
        Map<Class<? extends Exception>, Supplier<ServiceException.Code>> destination =
                ServiceExceptionHelper.putDefaultDestination(null);
        destination = com.dwarfeng.tmpstg.sdk.exception.ServiceExceptionHelper.putDefaultDestination(destination);
        return new MapServiceExceptionMapper(
                destination, com.dwarfeng.subgrade.basic.sdk.exception.ServiceExceptionCodeSuppliers.UNDEFINED
        );
    }
}
