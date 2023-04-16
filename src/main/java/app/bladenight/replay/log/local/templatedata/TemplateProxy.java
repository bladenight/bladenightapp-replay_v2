package app.bladenight.replay.log.local.templatedata;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

public class TemplateProxy {

    public TemplateProxy(String templateName) {
        this.dataModel = new HashMap<String, Object>();
        this.templateName = templateName;
    }

    public void generate(File outputFile) {
        try {
            Template temp = getInstance().getTemplate(templateName);
            Writer writer = new FileWriter(outputFile);
            temp.process(dataModel, writer);
            writer.close();
        } catch (TemplateException e) {
            getLog().error("Failed to generate " + outputFile.getName(), e);
        } catch (IOException e) {
            getLog().error("Failed to generate " + outputFile.getName(), e);
        }
    }

    public void putData(String key, Object data) {
        dataModel.put(key, data);
    }

    static private Configuration getInstance() {
        if ( configuration == null )
            createInstance();
        return configuration;
    }

    static private void createInstance() {
        configuration = new Configuration();
        configuration.setClassForTemplateLoading(TemplateProxy.class, "/");
        configuration.setObjectWrapper(new DefaultObjectWrapper());
        configuration.setDefaultEncoding("UTF-8");
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
    }

    private Map<String, Object> dataModel;
    private String templateName;

    static private Configuration configuration;

    private static Log log;

    public static void setLog(Log log) {
        TemplateProxy.log = log;
    }

    protected static Log getLog() {
        if (log == null)
            setLog(LogFactory.getLog(TemplateProxy.class));
        return log;
    }
}
