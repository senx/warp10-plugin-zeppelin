//
//   Copyright 2018-2021  SenX S.A.S.
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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.thrift.TException;
import org.apache.zeppelin.interpreter.Interpreter;
import org.apache.zeppelin.interpreter.remote.RemoteInterpreterServer;

import io.warp10.warp.sdk.AbstractWarp10Plugin;

public class ZeppelinWarp10Plugin extends AbstractWarp10Plugin {

  public static final String DEFAULT_PORT = Integer.toString(8081); // Spells ZEPP
  public static final String DEFAULT_HOST = "127.0.0.1";
  public static final String DEFAULT_PORTRANGE = Integer.toString(9377) + ":" + Integer.toString(9377); // Spells ZEPP
  public static final String DEFAULT_GROUPID = "WarpScript";
  public static final String DEFAULT_SESSIONID = "";

  public static final String ZEPPELIN_EVENT_SERVER_HOST = "zeppelin.eventserver.host";
  public static final String ZEPPELIN_EVENT_SERVER_PORT = "zeppelin.eventserver.port";

  public static final String ZEPPELIN_REMOTEINTERPRETER_HOST = "zeppelin.remoteinterpreter.host";
  public static final String ZEPPELIN_REMOTEINTERPRETER_PORTRANGE = "zeppelin.remoteinterpreter.portrange";
  public static final String ZEPPELIN_INTERPRETER_GROUPID = "zeppelin.interpreter.groupid";
  public static final String ZEPPELIN_INTERPRETER_SESSIONID = "zeppelin.interpreter.sessionid";
  public static final String ZEPPELIN_STACK_MAXLIMITS = "zeppelin.stack.maxlimits";

  @Override
  public void init(Properties props) {
    //String host = props.getProperty(ZEPPELIN_REMOTEINTERPRETER_HOST, DEFAULT_HOST);
    //int port = Integer.parseInt(props.getProperty(ZEPPELIN_REMOTEINTERPRETER_PORT, DEFAULT_PORT));
    String host = props.getProperty(ZEPPELIN_EVENT_SERVER_HOST, DEFAULT_HOST);
    int port = Integer.parseInt(props.getProperty(ZEPPELIN_EVENT_SERVER_PORT, DEFAULT_PORT));
    String groupid = props.getProperty(ZEPPELIN_INTERPRETER_GROUPID, DEFAULT_GROUPID);
    String portRange = props.getProperty(ZEPPELIN_REMOTEINTERPRETER_PORTRANGE, DEFAULT_PORTRANGE);

    try {

      // Don't specify a callback host, simply a port
      RemoteInterpreterServer ri = new RemoteInterpreterServer(host, port, portRange, groupid, false) {
        //
        // We need to override shutdown, otherwise a restart of the interpreter will
        // exit the current JVM.
        // @see ZEPPELIN-3565
        //
        @Override
        public void shutdown() throws TException {
          return;
        }

        @Override
        protected Interpreter getInterpreter(String sessionId, String className) throws TException {
          if (null == getInterpreterGroup()) {
            Map<String,String> properties = new HashMap<String,String>();
            for (Entry<Object,Object> entry: props.entrySet()) {
              properties.put(entry.getKey().toString(), entry.getValue().toString());
            }
            super.createInterpreter(groupid, sessionId, className, properties, null);
          }
          return super.getInterpreter(sessionId, className);
        }

        @Override
        public List<String> resourcePoolGetAll() throws TException {
          List<String> l = super.resourcePoolGetAll();
          return l;
        }
      };

      ri.setDaemon(true);
      ri.start();
      ri.setName("[WarpScript Zeppelin Remote Interpreter on port " + ri.getPort() + "]");
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
}
