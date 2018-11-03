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
package io.warp10.script.ext.zeppelin;

import org.apache.zeppelin.display.AngularObject;
import org.apache.zeppelin.display.AngularObjectRegistry;
import org.apache.zeppelin.interpreter.InterpreterContext;

import io.warp10.script.NamedWarpScriptFunction;
import io.warp10.script.WarpScriptException;
import io.warp10.script.WarpScriptStack;
import io.warp10.script.WarpScriptStackFunction;

/**
 * Load a resource from the Zeppelin Angular Registry and place it on the stack
 */
public class ZALOAD extends NamedWarpScriptFunction implements WarpScriptStackFunction {
  
  private final boolean bynotebook;
  private final boolean byparagraph;
  
  public ZALOAD(String name, boolean bynotebook, boolean byparagraph) {
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

    String noteId = bynotebook ? context.getNoteId() : null;
    String paragraphId = byparagraph ? context.getParagraphId() : null;
    
    AngularObject obj = aor.get(noteId, paragraphId, rscname);

    if (null == obj) {
      throw new WarpScriptException(getName() + " Zeppelin resource '" + rscname + "' not found.");
    }
    
    stack.push(obj.get());
    
    return stack;
  }

}
