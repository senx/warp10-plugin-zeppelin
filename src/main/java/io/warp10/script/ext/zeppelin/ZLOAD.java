package io.warp10.script.ext.zeppelin;

import org.apache.zeppelin.interpreter.InterpreterContext;
import org.apache.zeppelin.resource.Resource;
import org.apache.zeppelin.resource.ResourcePool;

import io.warp10.script.NamedWarpScriptFunction;
import io.warp10.script.WarpScriptException;
import io.warp10.script.WarpScriptStack;
import io.warp10.script.WarpScriptStackFunction;

/**
 * Load a resource from the Zeppelin Resource Pool and place it on the stack
 */
public class ZLOAD extends NamedWarpScriptFunction implements WarpScriptStackFunction {
  
  private final boolean bynotebook;
  private final boolean byparagraph;
  
  public ZLOAD(String name, boolean bynotebook, boolean byparagraph) {
    super(name);

    this.bynotebook = bynotebook;
    this.byparagraph = byparagraph;
  }
  
  @Override
  public Object apply(WarpScriptStack stack) throws WarpScriptException {
    
    ResourcePool rp = (ResourcePool) stack.getAttribute(ZeppelinWarpScriptExtension.ATTRIBUTE_ZEPPELIN_RESOURCE_POOL);
    
    if (null == rp) {
      throw new WarpScriptException(getName() + " Zeppelin Resource Pool unset.");
    }
    
    InterpreterContext context = (InterpreterContext) stack.getAttribute(ZeppelinWarpScriptExtension.ATTRIBUTE_ZEPPELIN_INTERPRETER_CONTEXT);
    
    if (null == context) {
      throw new WarpScriptException(getName() + " Zeppelin Interpreter Context unset.");
    }

    Object top = stack.pop();
    
    if (!(top instanceof String)) {
      throw new WarpScriptException(getName() + " expects a resource name on top of the stack.");
    }
    
    String rscname = top.toString();

    String noteId = bynotebook ? context.getNoteId() : null;
    String paragraphId = byparagraph ? context.getParagraphId() : null;
    
    Resource rsc = rp.get(noteId, paragraphId, rscname);

    if (null == rsc) {
      throw new WarpScriptException(getName() + " Zeppelin resource '" + rscname + "' not found.");
    }
    
    stack.push(rsc.get());
    
    return stack;
  }

}
