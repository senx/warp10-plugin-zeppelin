package io.warp10.script.ext.zeppelin;

import org.apache.zeppelin.interpreter.InterpreterContext;

import io.warp10.script.NamedWarpScriptFunction;
import io.warp10.script.WarpScriptException;
import io.warp10.script.WarpScriptStack;
import io.warp10.script.WarpScriptStackFunction;

/**
 * Puts the paragraphid on the stack
 */
public class ZPARAGRAPHID extends NamedWarpScriptFunction implements WarpScriptStackFunction {
  public ZPARAGRAPHID(String name) {
    super(name);
  }
  
  @Override
  public Object apply(WarpScriptStack stack) throws WarpScriptException {
    
    InterpreterContext context = (InterpreterContext) stack.getAttribute(ZeppelinWarpScriptExtension.ATTRIBUTE_ZEPPELIN_INTERPRETER_CONTEXT);
    
    if (null == context) {
      throw new WarpScriptException(getName() + " Zeppelin Interpreter Context unset.");
    }
    
    stack.push(context.getParagraphId());
    
    return stack;
  }

}
