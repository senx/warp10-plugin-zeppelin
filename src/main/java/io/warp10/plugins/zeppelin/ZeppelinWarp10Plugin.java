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
package io.warp10.plugins.zeppelin;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.thrift.TException;
import org.apache.zeppelin.interpreter.remote.RemoteInterpreterServer;

import io.warp10.warp.sdk.AbstractWarp10Plugin;

public class ZeppelinWarp10Plugin extends AbstractWarp10Plugin {

  public static final String DEFAULT_PORT = Integer.toString(9377); // Spells ZEPP
  public static final String DEFAULT_HOST = null;
  public static final String DEFAULT_PORTRANGE = "";
  public static final String DEFAULT_GROUPID = "WarpScript";
  public static final String DEFAULT_SESSIONID = "";

  public static final String ZEPPELIN_REMOTEINTERPRETER_PORT = "zeppelin.remoteinterpreter.port";
  public static final String ZEPPELIN_INTERPRETER_GROUPID = "zeppelin.interpreter.groupid";
  public static final String ZEPPELIN_INTERPRETER_SESSIONID = "zeppelin.interpreter.sessionid";
  public static final String ZEPPELIN_STACK_MAXLIMITS = "zeppelin.stack.maxlimits";

  @Override
  public void init(Properties props) {
    int port = Integer.parseInt(props.getProperty(ZEPPELIN_REMOTEINTERPRETER_PORT, DEFAULT_PORT));
    String groupid = props.getProperty(ZEPPELIN_INTERPRETER_GROUPID, DEFAULT_GROUPID);

    try {

      // Don't specify a callback host, simply a port
      RemoteInterpreterServer ri = new RemoteInterpreterServer(null, port, DEFAULT_PORTRANGE) {
        //
        // We need to override shutdown, otherwise a restart of the interpreter will
        // exit the current JVM.
        // @see ZEPPELIN-3565
        //
        @Override
        public void shutdown() throws TException {
          return;
        }
      };

      String sessionId = props.getProperty(ZEPPELIN_INTERPRETER_SESSIONID, DEFAULT_SESSIONID);
      String userName = null;

      Map<String,String> properties = new HashMap<String,String>();

      for (Entry<Object,Object> entry: props.entrySet()) {
        properties.put(entry.getKey().toString(), entry.getKey().toString());
      }
      ri.setDaemon(true);
      ri.start();
      ri.setName("[WarpScript Zeppelin Remote Interpreter on port " + ri.getPort() + "]");
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
}
