package io.warp10.script.ext.zeppelin;

import org.apache.zeppelin.display.AngularObjectRegistry;
import org.apache.zeppelin.interpreter.InterpreterContext;

import io.warp10.script.NamedWarpScriptFunction;
import io.warp10.script.WarpScriptException;
import io.warp10.script.WarpScriptStack;
import io.warp10.script.WarpScriptStackFunction;

/**
 * Store an object as a Zeppelin Resource in the Angular Object Registry
 */
public class ZASTORE extends NamedWarpScriptFunction implements WarpScriptStackFunction {
  
  private final boolean bynotebook;
  private final boolean byparagraph;
  
  public ZASTORE(String name, boolean bynotebook, boolean byparagraph) {
    super(name);
    this.bynotebook = bynotebook;
    this.byparagraph = byparagraph;
  }
  
  @Override
  public Object apply(WarpScriptStack stack) throws WarpScriptException {
    
    AngularObjectRegistry aor = (AngularObjectRegistry) stack.getAttribute(ZeppelinWarpScriptExtension.ATTRIBUTE_ZEPPELIN_ANGULAR_REGISTRY);
    
    if (null == aor) {
      throw new WarpScriptException(getName() + " Zeppelin Angular Object Registry unset.");
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
      aor.add(rscname, top, noteId, paragraphId);
    } else {
      aor.remove(rscname, noteId, paragraphId);
    }
    
    return stack;
  }

}
