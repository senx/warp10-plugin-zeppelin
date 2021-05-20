# Integrating Warp 10™ with Apache Zeppelin

## Overview

[Apache Zeppelin](https://zeppelin.apache.org/) is a web based notebook tool for data analysis.

Zeppelin organizes its notebooks in `paragraphs` which call `interpreters` to execute their content. The integration of Warp 10™ with Zeppelin defines a new interpreter able to execute WarpScript™ code either locally or remotely.

## Required code

The integration of Warp 10™ and Zeppelin is done through the use of a plugin. The source code for this plugin is on [GitHub](https://github.com/senx/warp10-plugin-zeppelin).

The Gradle `shadowJar` task builds a jar file in `build/libs` with the name `warp10-plugin-zeppelin.jar`.

This jar file is all that is required for a remote Warp 10™ interpreter. For a local interpreter, the jar file for Warp 10™ is also needed, you can download it from Bintray and extract it from the `tar` file. [ ![Download](https://api.bintray.com/packages/senx/generic/warp10/images/download.svg) ](https://bintray.com/senx/generic/warp10/_latestVersion)

## Local interpreter

A local interpreter executes WarpScript™ code in a JVM running on the same machine as Zeppelin. This JVM is launched by Zeppelin itself.

In order to define a local Warp 10™ interpreter, you must deploy both the `warp10-plugin-zeppelin.jar` and the Warp 10™ jar into a newly created `WarpScript` directory in the `interpreter` directory of the Zeppelin installation.

You must also create a simple Warp 10™ configuration file and ensure that either the `WARP10_CONFIG` environment variable or the `warp10.config` JVM property points to the absolute path to this file when Zeppelin launches the external JVM for your new interpreter.

One way to ensure this is to modify the file `conf/zeppelin-env.sh` in the Zeppelin installation to define the following environment variable:

```bash
export ZEPPELIN_INTP_JAVA_OPTS=-Dwarp10.config=/path/to/warp10.conf
```

A local WarpScript™ interpreter is not linked to a Warp 10™ instance and therefore does not have any datastore it can interact with. For this reason it is highly advised that you enable the use of the `REXEC` function via the configuration property `warpscript.rexec.enable` set to `true` in your Warp 10™ configuration file.

It is also greatly advised that you load the Zeppelin WarpScript™ extension in your Warp 10™ configuration file.

The following minimalist configuration file works well:

```properties
warp.timeunits=us
warpscript.extension.zeppelin=io.warp10.script.ext.zeppelin.ZeppelinWarpScriptExtension
warpscript.rexec.enable = true
```

To define a WarpScript™ local interpreter in Zeppelin, simply add an interpreter in the `WarpScript` group in the `http://<host>:<port>/#/interpreter` page of the Zeppelin application.

## Remote interpreter

A remote interpreter executes WarpScript™ code in an existing Warp 10™ instance, the Zeppelin application does not launch a specific JVM for such an interpreter.

On the Warp 10™ instance, the `warp10-plugin-zeppelin.jar` must be deployed in the classpath, for example by putting it in the `lib` directory of your Warp 10™ installation. The configuration must then be changed to load the plugin at startup:

```properties
warp10.plugin.zeppelin = io.warp10.plugins.zeppelin.ZeppelinWarp10Plugin
```

In Zeppelin, a remote interpreter is defined by creating a `WarpScript` directory under the `interpreter` directory of the Zeppelin installation and deploying the `warp10-plugin-zeppelin.jar` archive in it.

The interpreter is then defined in the `http://<host>:<port>/#/interpreter` page of the Zeppelin application under the `WarpScript` interpreter group with the option of connecting to an existing process where you need to fill the host and port of the Warp 10™ instance you wish to connect to.

The default port the plugin listens to is 9377 (spells ZEPP on a phone), if you wish to use a different port, set the property `zeppelin.remoteinterpreter.port` to the desired port in the Warp 10™ configuration file.

In order to fetch data from the remote instance, you have to expose the egress:
```properties
//
// Should the egress exec handler expose its store/directory clients?
//
egress.clients.expose = true
```

## Zeppelin WarpScript™ extension

In order to improve the integration between WarpScript™ and Zeppelin, a dedicated extension has been created. This extension adds functions allowing to read and write resources in both the Zeppelin Resource Pool and the Angular Registry.

### Interacting with the Zeppelin Resource Pool

The `ZLOAD` and `ZSTORE` functions will read and write global resources in the Zeppelin Resource Pool. The values stored in those resources must be serializable by Zeppelin.

The `ZNLOAD` and `ZNSTORE` variants of those functions allow to get and set resources with a scope limited to a given note.

The `ZPLOAD` and `ZPSTORE` variants allow to get and set resources with a scope limited to a specific paragraph in a note.

The resources manipulated by those functions can be accessed in Zeppelin via `z.z.get(rsc)` and `z.z.put(rsc,value)` in Python.

### Interacting with the Angular Registry

The `ZALOAD` and `ZASTORE` functions will read and write global resources in the Zeppelin Angular Registry. The values stored in those resources must be serializable by Zeppelin.

As for the Zeppelin Resource Pool, the variants `ZANLOAD`, `ZANSTORE`, `ZAPLOAD` and `ZAPSTORE` manipulate Angular Registry resources with scopes limited to a note or a paragraph.

The resources manipulated by those functions can be accessed in Zeppelin via the `{{rsc}}` Angular construct and via `z.z.angularBind` in Python.

### Other functions

The `ZNOTEID` and `ZPARAGRAPHID` functions will retrieve the note and paragraph id of the entity executing the current WarpScript™ code.

The `ZLEVELS` function will instruct the WarpScript™ interpreter to return separate Zeppelin results for each level of the stack instead of a single result with the JSON representation of the stack.

This latter possibility, coupled with the special formatting described below allows you to return different types of results from a single WarpScript™ code fragment.

## Special formatting of output

The WarpScript™ interpreter can return results to Zeppelin with special formatting options to instruct Zeppelin to interpret the results in specific ways.

The rest of this documentation assumes you have called `ZLEVELS` in your script.

If a stack level contains an image, such as one created by `Pencode`, Zeppelin will display it as such.

If a stack level contains a STRING beginning with `#html `, the rest of the character string will be interpreted as HTML (`%html`).

If a stack level contains a STRING beginning with `#angular `, the rest of it will be interpreted as `%angular`.

If a stack level contains a STRING beginning with `#table `, the rest will be interpreted as tabular data that consists in a header line and data lines. Columns are separated by tabulations (`%09`) and lines are separated by newlines (`%0a`).

Any other content will be treated as text.
