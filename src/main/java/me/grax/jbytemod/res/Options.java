package me.grax.jbytemod.res;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import com.strobel.decompiler.DecompilerSettings;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.decompiler.CFRDecompiler;
import me.grax.jbytemod.decompiler.FernflowerDecompiler;
import me.grax.jbytemod.res.Option.Type;
import me.grax.jbytemod.utils.ErrorDisplay;

public class Options {
  private static final File propFile = new File(JByteMod.workingDir, JByteMod.configPath);

  public List<Option> bools = new ArrayList<>();
  public List<Option> defaults = new ArrayList<>(Arrays.asList(new Option("sort_methods", false, Type.BOOLEAN),
      new Option("use_rt", false, Type.BOOLEAN), new Option("compute_maxs", true, Type.BOOLEAN), new Option("select_code_tab", true, Type.BOOLEAN),
      new Option("memory_warning", true, Type.BOOLEAN), new Option("python_path", "", Type.STRING),
      new Option("hints", false, Type.BOOLEAN, "editor"), new Option("copy_formatted", false, Type.BOOLEAN, "editor"),
      new Option("analyze_errors", true, Type.BOOLEAN, "editor"), new Option("simplify_graph", true, Type.BOOLEAN, "graph"),
      new Option("remove_redundant", false, Type.BOOLEAN, "graph"), new Option("max_redundant_input", 2, Type.INT, "graph"),
      new Option("decompile_graph", true, Type.BOOLEAN, "graph"), new Option("primary_color", "#557799", Type.STRING, "color"),
      new Option("secondary_color", "#995555", Type.STRING, "color"), new Option("use_weblaf", true, Type.BOOLEAN, "style")));

  public Options() {
    initializeDecompilerOptions();
    if (propFile.exists()) {
      JByteMod.LOGGER.log("Loading settings... ");
      try {
        Files.lines(propFile.toPath()).forEach(l -> {
          int split = l.indexOf('=');
          String part1 = l.substring(0, split);
          String part2 = split == l.length() ? "" : l.substring(split + 1, l.length());
          String[] def = part1.split(":");
          try {
            bools.add(new Option(def[0], part2, Type.valueOf(def[1]), def[2]));
          } catch (Exception e) {
            JByteMod.LOGGER.warn("Couldn't parse line: " + l);
          }
        });
        for (int i = 0; i < bools.size(); i++) {
          Option o1 = bools.get(i);
          Option o2 = defaults.get(i);
          if (o1 == null || o2 == null || find(o2.getName()) == null || findDefault(o1.getName()) == null) {
            JByteMod.LOGGER.warn("Option file not matching defaults, maybe from old version?");
            this.initWithDefaults(true);
            this.save();
            return;
          }
        }
        if (bools.isEmpty()) {
          JByteMod.LOGGER.warn("Couldn't read file, probably empty");
          this.initWithDefaults(false);
          this.save();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      JByteMod.LOGGER.warn("Property File \"" + propFile.getName() + "\" does not exist, creating...");
      this.initWithDefaults(false);
      this.save();
    }
  }

  private void initializeDecompilerOptions() {
    for (Entry<String, Boolean> e : FernflowerDecompiler.options.entrySet()) {
      defaults.add(new Option("ff_" + e.getKey(), e.getValue(), Type.BOOLEAN, "decompiler_fernflower"));
    }
    for (Entry<String, String> e : CFRDecompiler.options.entrySet()) {
      defaults.add(new Option("cfr_" + e.getKey(), Boolean.valueOf(e.getValue()), Type.BOOLEAN, "decompiler_cfr"));
    }
    try {
      DecompilerSettings s = new DecompilerSettings();
      for (Field f : s.getClass().getDeclaredFields()) {
        if (f.getType() == boolean.class) {
          f.setAccessible(true);
          defaults.add(new Option("procyon" + f.getName(), f.getBoolean(s), Type.BOOLEAN, "decompiler_procyon"));
        }
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  private void initWithDefaults(boolean keepExisting) {
    if (keepExisting) {
      for (Option o : defaults) {
        if (find(o.getName()) == null) {
          bools.add(o);
        }
      }
      for (Option o : new ArrayList<>(bools)) {
        if (findDefault(o.getName()) == null) {
          bools.remove(o);
        }
      }
    } else {
      bools = new ArrayList<>();
      bools.addAll(defaults);
    }
  }

  public void save() {
    new Thread(() -> {
      try {
        if (!propFile.exists()) {
          propFile.getParentFile().mkdirs();
          propFile.createNewFile();
          JByteMod.LOGGER.log("Prop file doesn't exist, creating.");
        }
        PrintWriter pw = new PrintWriter(propFile);
        for (Option o : bools) {
          pw.println(o.getName() + ":" + o.getType().name() + ":" + o.getGroup() + "=" + o.getValue());
        }
        pw.close();
      } catch (Exception e) {
        new ErrorDisplay(e);
      }
    }).start();
  }

  public Option get(String name) {
    Option op = find(name);
    if (op != null) {
      return op;
    }
    JOptionPane.showMessageDialog(null, "Missing option: " + name + "\nRewriting your config file!");
    this.initWithDefaults(false);
    this.save();
    op = find(name);
    if (op != null) {
      return op;
    }
    throw new RuntimeException("Option not found: " + name);
  }

  private Option find(String name) {
    for (Option o : bools) {
      if (o.getName().equalsIgnoreCase(name)) {
        return o;
      }
    }
    return null;
  }

  private Option findDefault(String name) {
    for (Option o : defaults) {
      if (o.getName().equalsIgnoreCase(name)) {
        return o;
      }
    }
    return null;
  }

}
