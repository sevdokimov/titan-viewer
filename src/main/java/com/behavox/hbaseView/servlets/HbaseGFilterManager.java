package com.behavox.hbaseView.servlets;

import groovy.lang.GroovyClassLoader;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.util.Bytes;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.groovy.jsr223.GroovyScriptEngineImpl;

import javax.annotation.Nullable;
import javax.script.CompiledScript;
import javax.script.ScriptException;

/**
 *
 */
public class HbaseGFilterManager {

    private static final HbaseGFilterManager INSTANCE = new HbaseGFilterManager();

    private final GroovyScriptEngineImpl engine;

    private HbaseGFilterManager() {
        CompilerConfiguration compilerCfg = new CompilerConfiguration();

        ImportCustomizer ic = new ImportCustomizer();
        ic.addStarImports("org.apache.hadoop.hbase.filter");
        ic.addImports(Bytes.class.getName(), "org.apache.hadoop.hbase.filter.CompareFilter.CompareOp");
        ic.addStaticStars(Bytes.class.getName());

        compilerCfg.addCompilationCustomizers(ic);

        GroovyClassLoader groovyClassLoader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader(),
                compilerCfg);

        engine = new GroovyScriptEngineImpl(groovyClassLoader);
    }

    public static HbaseGFilterManager getInstance() {
        return INSTANCE;
    }

    @Nullable
    public Filter evaluateFilter(@Nullable String code) throws ScriptException {
        if (code == null)
            return null;

        code = code.trim();

        if (isEmptyOrComment(code))
            return null;

        Object res = engine.eval(code);

        if (res == null)
            return null;

        if (!Filter.class.isInstance(res)) {
            throw new ScriptException("Result is not an instance of org.apache.hadoop.hbase.filter.Filter");
        }

        return (Filter) res;
    }

    public static boolean isEmptyOrComment(String code) {
        if (code == null)
            return true;

        for (String s : code.split("\\n")) {
            s = s.trim();

            if (!s.isEmpty() && !s.startsWith("//")) {
                return false;
            }
        }

        return true;
    }
}
