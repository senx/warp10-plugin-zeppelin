package io.warp10.script.ext.zeppelin;

import java.util.HashMap;
import java.util.Map;

import io.warp10.warp.sdk.WarpScriptExtension;

public class ZeppelinWarpScriptExtension extends WarpScriptExtension {
  
  private static final Map<String,Object> functions;
  
  public static final String ATTRIBUTE_ZEPPELIN_RESOURCE_POOL = "zeppelin.resource.pool";
  public static final String ATTRIBUTE_ZEPPELIN_INTERPRETER_CONTEXT = "zeppelin.interpreter.context";
  public static final String ATTRIBUTE_ZEPPELIN_ANGULAR_REGISTRY = "zeppelin.angular.registry";
  public static final String ATTRIBUTE_ZEPPELIN_LEVELS = "zeppelin.levels";
  
  static {
    functions = new HashMap<String,Object>();
    
    functions.put("ZLOAD", new ZLOAD("ZLOAD", false, false));
    functions.put("ZSTORE", new ZSTORE("ZSTORE", false, false));
    functions.put("ZNLOAD", new ZLOAD("ZNLOAD", true, false));
    functions.put("ZNSTORE", new ZSTORE("ZNSTORE", true, false));
    functions.put("ZPLOAD", new ZLOAD("ZPLOAD", true, true));
    functions.put("ZPSTORE", new ZSTORE("ZPSTORE", true, true));
    functions.put("ZALOAD", new ZALOAD("ZALOAD", false, false));
    functions.put("ZASTORE", new ZASTORE("ZASTORE", false, false));
    functions.put("ZANLOAD", new ZALOAD("ZANLOAD", true, false));
    functions.put("ZANSTORE", new ZASTORE("ZANSTORE", true, false));
    functions.put("ZAPLOAD", new ZALOAD("ZAPLOAD", true, true));
    functions.put("ZAPSTORE", new ZASTORE("ZAPSTORE", true, true));
    functions.put("ZNOTEID", new ZNOTEID("ZNOTEID"));
    functions.put("ZPARAGRAPHID", new ZPARAGRAPHID("ZPARAGRAPHID"));
    functions.put("ZLEVELS", new ZLEVELS("ZLEVELS"));
  }
  
  @Override
  public Map<String, Object> getFunctions() {
    return functions;
  }

}
