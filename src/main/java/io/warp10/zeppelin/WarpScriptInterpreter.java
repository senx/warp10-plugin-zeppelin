//
//   Copyright 2018  SenX S.A.S.
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//
package io.warp10.zeppelin;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import io.warp10.ThrowableUtils;
import org.apache.zeppelin.interpreter.Interpreter;
import org.apache.zeppelin.interpreter.InterpreterContext;
import org.apache.zeppelin.interpreter.InterpreterResult;
import org.apache.zeppelin.interpreter.InterpreterResult.Code;
import org.apache.zeppelin.interpreter.InterpreterResult.Type;
import org.apache.zeppelin.scheduler.Scheduler;
import org.apache.zeppelin.scheduler.SchedulerFactory;
import org.apache.zeppelin.interpreter.InterpreterResultMessage;

import io.warp10.WarpConfig;
import io.warp10.plugins.zeppelin.ZeppelinWarp10Plugin;
import io.warp10.script.MemoryWarpScriptStack;
import io.warp10.script.StackUtils;
import io.warp10.script.WarpScriptException;
import io.warp10.script.WarpScriptLib;
import io.warp10.script.ext.zeppelin.ZeppelinWarpScriptExtension;

public class WarpScriptInterpreter extends Interpreter {

  private Properties properties;
  
  private static final String PROPERTY_SCHEDULER_NAME = "warpscript.zeppelin.scheduler.name";
  private static final String PROPERTY_SCHEDULER_TYPE = "warpscript.zeppelin.scheduler.type";
  private static final String PROPERTY_SCHEDULER_MAXCONCURRENCY = "warpscript.zeppelin.scheduler.maxconcurrency";
  
  static {
    try {
      if (null != System.getProperty(WarpConfig.WARP10_CONFIG)) {
        WarpConfig.safeSetProperties(System.getProperty(WarpConfig.WARP10_CONFIG));
      } else if (null != System.getenv(WarpConfig.WARP10_CONFIG_ENV)) {
        WarpConfig.safeSetProperties(System.getenv(WarpConfig.WARP10_CONFIG_ENV));
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
    
    MemoryWarpScriptStack stack = new MemoryWarpScriptStack(ZeppelinWarp10Plugin.getExposedStoreClient(), ZeppelinWarp10Plugin.getExposedDirectoryClient(), properties);
    
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
    } catch (WarpScriptException | IOException e) {
      error = e;
    }

    return new InterpreterResult(Code.ERROR, Type.TEXT, ThrowableUtils.getErrorMessage(error));
  }

  @Override
  public void open() {
  }
  
  @Override
  public Scheduler getScheduler() {    
    String type = this.properties.getOrDefault(PROPERTY_SCHEDULER_TYPE, "parallel").toString();
    String name = this.properties.getOrDefault(PROPERTY_SCHEDULER_NAME, "WarpScriptZeppelinScheduler").toString();
    int maxconcurrency = Integer.parseInt(this.properties.getOrDefault(PROPERTY_SCHEDULER_MAXCONCURRENCY, "128").toString());
    if ("FIFO".equalsIgnoreCase(type)) {
      return SchedulerFactory.singleton().createOrGetFIFOScheduler(name);
    } else {
      return SchedulerFactory.singleton().createOrGetParallelScheduler(name, maxconcurrency);
    }
  }
}