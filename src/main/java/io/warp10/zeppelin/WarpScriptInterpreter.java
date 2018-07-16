package io.warp10.zeppelin;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.zeppelin.interpreter.Interpreter;
import org.apache.zeppelin.interpreter.InterpreterContext;
import org.apache.zeppelin.interpreter.InterpreterResult;
import org.apache.zeppelin.interpreter.InterpreterResult.Code;
import org.apache.zeppelin.interpreter.InterpreterResult.Type;
import org.apache.zeppelin.interpreter.InterpreterResultMessage;

import io.warp10.WarpConfig;
import io.warp10.plugins.zeppelin.ZeppelinWarp10Plugin;
import io.warp10.script.MemoryWarpScriptStack;
import io.warp10.script.StackUtils;
import io.warp10.script.WarpScriptException;
import io.warp10.script.WarpScriptLib;
import io.warp10.script.ext.zeppelin.ZeppelinWarpScriptExtension;

public class WarpScriptInterpreter extends Interpreter {

  private static final String PROP_WARP10_CONFIG = "warp10.config";
  private static final String ENV_WARP10_CONFIG = "WARP10_CONFIG";
  
  private Properties properties;
  
  static {
    try {
      if (null != System.getProperty(PROP_WARP10_CONFIG)) {
        WarpConfig.safeSetProperties(System.getProperty(PROP_WARP10_CONFIG));        
      } else if (null != System.getenv(ENV_WARP10_CONFIG)) {
        WarpConfig.safeSetProperties(System.getenv(ENV_WARP10_CONFIG));
      } else {
        WarpConfig.safeSetProperties((String) null);
      }
      if (!WarpScriptLib.extloaded(ZeppelinWarpScriptExtension.class.getCanonicalName())) {
        WarpScriptLib.registerExtensions();
        WarpScriptLib.register(new ZeppelinWarpScriptExtension());
      }
    } catch (IOException ioe) {      
    }
  }
  
  public WarpScriptInterpreter(Properties properties) {
    super(properties);
    this.properties = properties;
  }
  
  @Override
  public void cancel(InterpreterContext context) {
  }

  @Override
  public void close() {
  }

  @Override
  public FormType getFormType() {
    return FormType.SIMPLE;
  }

  @Override
  public int getProgress(InterpreterContext context) {
    return 0;
  }

  @Override
  public InterpreterResult interpret(String script, InterpreterContext context) {

    Properties properties = WarpConfig.getProperties();
    
    // Override properties with those from the interpreter
    properties.putAll(this.properties);
    
    //MemoryWarpScriptStack stack = new MemoryWarpScriptStack(ZeppelinWarp10Plugin.getExposedStoreClient(), ZeppelinWarp10Plugin.getExposedDirectoryClient(), properties);
    MemoryWarpScriptStack stack = new MemoryWarpScriptStack(null, null, properties);
    
    if ("true".equals(properties.getProperty(ZeppelinWarp10Plugin.ZEPPELIN_STACK_MAXLIMITS))) {
      stack.maxLimits();
    }
    
    Throwable error = null;
    
    stack.setAttribute(ZeppelinWarpScriptExtension.ATTRIBUTE_ZEPPELIN_INTERPRETER_CONTEXT, context);
    stack.setAttribute(ZeppelinWarpScriptExtension.ATTRIBUTE_ZEPPELIN_RESOURCE_POOL, context.getResourcePool());
    stack.setAttribute(ZeppelinWarpScriptExtension.ATTRIBUTE_ZEPPELIN_ANGULAR_REGISTRY, context.getAngularObjectRegistry());
    
    try {
      stack.execMulti(script);     
    } catch (Throwable t) {
      error = t;
    }

    try {

      if (null == error) {
        if (Boolean.TRUE.equals(stack.getAttribute(ZeppelinWarpScriptExtension.ATTRIBUTE_ZEPPELIN_LEVELS))) {
          
          List<InterpreterResultMessage> results = new ArrayList<InterpreterResultMessage>();
          
          for (int i = 0; i < stack.depth(); i++) {
            Object obj = stack.get(i);
            
            Type type = Type.NULL;
            
            String data = null;
            
            if (obj instanceof String) {
              String str = String.valueOf(obj);
              
              if (str.startsWith("#html ")) {
                type = Type.HTML;
                data = str.substring(6);
              } else if (str.startsWith("#angular ")) {
                type = Type.ANGULAR;
                data = str.substring(9);
              } else if (str.startsWith("#table ")) {
                type = Type.TABLE;
                data = str.substring(7);
              } else if (str.startsWith("data:image/png;base64,")) {
                type = Type.IMG;
                data = str.substring(22);
              } else {
                type = Type.TEXT;
                data = str;
              }
            } else {
              type = Type.TEXT;
              data = String.valueOf(obj);
            }
            
            results.add(new InterpreterResultMessage(type, data));
          }
          
          return new InterpreterResult(Code.SUCCESS, results);
        } else {
          StringWriter json = new StringWriter();
          PrintWriter out = new PrintWriter(json);
                
          StackUtils.toJSON(out, stack);
          return new InterpreterResult(Code.SUCCESS, Type.TEXT, json.toString());
        }
      }      
    } catch (WarpScriptException wse) {
      error = wse;
    }        
    
    return new InterpreterResult(Code.ERROR, Type.TEXT, error.getMessage());
  }

  @Override
  public void open() {
  }
}