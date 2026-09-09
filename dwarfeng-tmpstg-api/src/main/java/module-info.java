module com.dwarfeng.tmpstg.api {

    requires com.dwarfeng.tmpstg.core;
    requires com.dwarfeng.dutil.basic;
    requires com.dwarfeng.springtelqos.core;
    requires org.apache.commons.cli;
    requires org.apache.commons.lang3;
    requires org.jetbrains.annotations;

    exports com.dwarfeng.tmpstg.api.integration.springtelqos;
}
