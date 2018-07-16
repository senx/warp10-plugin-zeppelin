package io.warp10.script.ext.zeppelin;

import org.apache.zeppelin.interpreter.InterpreterContext;
import org.apache.zeppelin.resource.ResourcePool;

import io.warp10.script.NamedWarpScriptFunction;
import io.warp10.script.WarpScriptException;
import io.warp10.script.WarpScriptStack;
import io.warp10.script.WarpScriptStackFunction;

/**
 * Store an object as a Zeppelin Resource in the Zeppelin Resource Pool
 */
public class ZSTORE extends NamedWarpScriptFunction implements WarpScriptStackFunction {
  
  private final boolean bynotebook;
  private final boolean byparagraph;
  
  public ZSTORE(String name, boolean bynotebook, boolean byparagraph) {
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
    
    top = stack.pop();
    
    String noteId = this.bynotebook ? context.getNoteId() : null;
    String paragraphId = this.byparagraph ? context.getParagraphId() : null;
    
    if (null != top) {
      rp.put(noteId, paragraphId, rscname, top);
    } else {
      rp.remove(noteId, paragraphId, rscname);
    }
    
    return stack;
  }

}
