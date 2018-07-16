package io.warp10.script.ext.zeppelin;

import org.apache.zeppelin.interpreter.InterpreterContext;

import io.warp10.script.NamedWarpScriptFunction;
import io.warp10.script.WarpScriptException;
import io.warp10.script.WarpScriptStack;
import io.warp10.script.WarpScriptStackFunction;

/**
 * Changes the output as a per level one
 */
public class ZLEVELS extends NamedWarpScriptFunction implements WarpScriptStackFunction {
  public ZLEVELS(String name) {
    super(name);
  }
  
  @Override
  public Object apply(WarpScriptStack stack) throws WarpScriptException {

    stack.setAttribute(ZeppelinWarpScriptExtension.ATTRIBUTE_ZEPPELIN_LEVELS, Boolean.TRUE);
    return stack;
  }

}
