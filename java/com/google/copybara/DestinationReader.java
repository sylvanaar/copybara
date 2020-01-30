/*
 * Copyright (C) 2020 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.copybara;

import com.google.copybara.doc.annotations.Example;
import com.google.copybara.exception.RepoException;
import com.google.copybara.exception.ValidationException;
import com.google.copybara.util.Glob;
import com.google.devtools.build.lib.skylarkinterface.Param;
import com.google.devtools.build.lib.skylarkinterface.SkylarkCallable;
import com.google.devtools.build.lib.skylarkinterface.SkylarkModule;
import com.google.devtools.build.lib.skylarkinterface.SkylarkModuleCategory;
import com.google.devtools.build.lib.syntax.StarlarkValue;

/** A repository which a source of truth can be copied to. */
@SkylarkModule(
    name = "destination_reader",
    doc = "Handle to read from the destination",
    category = SkylarkModuleCategory.TOP_LEVEL_TYPE,
    documented = true)
public abstract class DestinationReader implements StarlarkValue {

  public static final DestinationReader NOT_IMPLEMENTED = new DestinationReader() {
    @Override
    public String readFile(String path) throws RepoException {
      throw new RepoException("Reading files is not implemented by this destination");
    }

    @Override
    public void copyDestinationFiles(Glob path) throws RepoException {
      throw new RepoException("Reading files is not implemented by this destination");
    }
  };

  public static final DestinationReader NOOP_DESTINATION_READER = new DestinationReader() {
    @Override
    public String readFile(String path) {
      return  "";
    }

    @Override
    public void copyDestinationFiles(Glob path) {
      return;
    }
  };

  @SkylarkCallable(
      name = "read_file",
      doc = "Read a file from the destination.",
      parameters = {
          @Param(name = "path", type = String.class, named = true, doc = "Path to the file."),
      })
  @Example(
      title = "Read a file from the destination's baseline",
      before = "This can be added to the transformations of your core.workflow:",
      code =
          "def _read_destination_file(ctx):\n"
              + "    content = ctx.destination_reader().read_file(path = path/to/my_file.txt')\n"
              + "    ctx.console.info(content)\n\n"
              + "    transforms = [core.dynamic_transform(_read_destination_file)]\n",
      after =
          "Would print out the content of path/to/my_file.txt in the destination. The file does not"
              + " have to be covered by origin_files nor destination_files.")
  @SuppressWarnings("unused")
  public abstract String readFile(String path) throws RepoException;

  @SkylarkCallable(
      name = "copy_destination_files",
      doc = "Copy files from the destination into the workdir.",
      parameters = {
          @Param(name = "glob", type = Glob.class, named = true, doc = "Files to copy to the "
              + "workdir, potentially overwriting files checked out from the origin."),
      })
  @Example(
      title = "Copy files from the destination's baseline",
      before = "This can be added to the transformations of your core.workflow:",
      code =
          "def _copy_destination_file(ctx):\n"
              + "    content = ctx.destination_reader().copy_destination_files(path = path/to/**')"
              + "\n\n"
              + "    transforms = [core.dynamic_transform(_copy_destination_file)]\n",
      after =
          "Would copy all files in path/to/ from the destination baseline to the copybara workdir."
              + " The files do not have to be covered by origin_files nor destination_files, but "
              + "will cause errors if they are not covered by destination_files and not moved or "
              + "deleted.")
  @SuppressWarnings("unused")
  public abstract void copyDestinationFiles(Glob glob) throws RepoException, ValidationException;
}
