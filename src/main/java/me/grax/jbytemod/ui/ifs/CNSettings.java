package me.grax.jbytemod.ui.ifs;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.objectweb.asm.tree.ClassNode;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.ui.JAccessHelper;
import me.grax.jbytemod.ui.JAnnotationEditor;
import me.grax.jbytemod.ui.JListEditor;
import me.grax.jbytemod.ui.dialogue.ClassDialogue;
import me.grax.jbytemod.utils.gui.SwingUtils;

public class CNSettings extends MyInternalFrame {
  /**
   * Save position
   */
  private static Rectangle bounds = new Rectangle(10, 10, 1280 / 4, 720 / 3 + 200);

  public CNSettings(ClassNode cn) {
    super("Class Settings");
    this.setBounds(bounds);
    this.setLayout(new BorderLayout(0, 0));
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout(5, 5));
    panel.setBorder(new EmptyBorder(10, 10, 10, 10));
    final JPanel input = new JPanel(new GridLayout(0, 1));
    final JPanel labels = new JPanel(new GridLayout(0, 1));
    panel.add(labels, "West");
    panel.add(input, "Center");
    panel.add(new JLabel(JByteMod.res.getResource("ref_warn")), "South");
    labels.add(new JLabel("Class Name:"));
    JTextField name = new JTextField(cn.name);
    input.add(name);
    labels.add(new JLabel("Class SourceFile:"));
    JTextField sf = new JTextField(cn.sourceFile);
    input.add(sf);
    labels.add(new JLabel("Class SourceDebug:"));
    JTextField sd = new JTextField(cn.sourceDebug);
    input.add(sd);
    labels.add(new JLabel("Class Access:"));
    JFormattedTextField access = ClassDialogue.createNumberField(Integer.class, 0, Short.MAX_VALUE);
    access.setValue(cn.access);
    input.add(SwingUtils.withButton(access, "...", e -> {
      JAccessHelper jah = new JAccessHelper(cn, "access", access);
      jah.setVisible(true);

    }));
    labels.add(new JLabel("Class Version:"));
    JFormattedTextField version = ClassDialogue.createNumberField(Integer.class, 0, Short.MAX_VALUE);
    version.setValue(cn.version);
    input.add(SwingUtils.withButton(version, "?", e -> {
      JOptionPane.showMessageDialog(this,
          "Java SE 9 = 53 (0x35 hex),\nJava SE 8 = 52 (0x34 hex),\nJava SE 7 = 51 (0x33 hex),\nJava SE 6.0 = 50 (0x32 hex),\nJava SE 5.0 = 49 (0x31 hex)");
    }));
    labels.add(new JLabel("Class Signature:"));
    JTextField signature = new JTextField(cn.signature);
    input.add(signature);
    labels.add(new JLabel("Class Parent:"));
    JTextField parent = new JTextField(cn.superName);
    input.add(parent);
    labels.add(new JLabel("Class Interfaces:"));
    JButton interfaces = new JButton(JByteMod.res.getResource("edit"));
    interfaces.addActionListener(a -> {
      if (!JListEditor.isOpen())
        new JListEditor("Interfaces", cn, "interfaces").setVisible(true);
    });
    input.add(interfaces);
    labels.add(new JLabel("Outer Class:"));
    JTextField outerclass = new JTextField(cn.outerClass);
    input.add(outerclass);
    labels.add(new JLabel("Outer Method:"));
    JTextField outermethod = new JTextField(cn.outerMethod);
    input.add(outermethod);
    labels.add(new JLabel("Outer Method Desc:"));
    JTextField outerdesc = new JTextField(cn.outerMethodDesc);
    input.add(outerdesc);
    labels.add(new JLabel("Annotations:"));
    JButton annotations = new JButton(JByteMod.res.getResource("edit"));
    annotations.addActionListener(a -> {
      if (!JAnnotationEditor.isOpen("visibleAnnotations"))
        new JAnnotationEditor("Annotations", cn, "visibleAnnotations").setVisible(true);
    });
    input.add(annotations);
    labels.add(new JLabel("Invisible Annotations:"));
    JButton invisAnnotations = new JButton(JByteMod.res.getResource("edit"));
    invisAnnotations.addActionListener(a -> {
      if (!JAnnotationEditor.isOpen("invisibleAnnotations"))
        new JAnnotationEditor("Invisible Annotations", cn, "invisibleAnnotations").setVisible(true);
    });
    input.add(invisAnnotations);
    this.add(panel, BorderLayout.CENTER);
    JButton update = new JButton("Update");
    update.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        boolean refresh = false;
        if (!cn.name.equals(name.getText())) {
          refresh = true;
          cn.name = name.getText();
        }
        cn.sourceFile = sf.getText();
        cn.access = (int) access.getValue();
        cn.version = (int) version.getValue();
        String srcDebug = sd.getText();
        if (srcDebug.isEmpty()) {
          cn.sourceDebug = null;
        } else {
          cn.sourceDebug = srcDebug;
        }
        String sig = signature.getText();
        if (sig.isEmpty()) {
          cn.signature = null;
        } else {
          cn.signature = sig;
        }
        String par = parent.getText();
        if (par.isEmpty()) {
          cn.superName = null;
        } else {
          cn.superName = par;
        }
        String oc = outerclass.getText();
        if (oc.isEmpty()) {
          cn.outerClass = null;
        } else {
          cn.outerClass = oc;
        }
        String om = outermethod.getText();
        if (om.isEmpty()) {
          cn.outerMethod = null;
        } else {
          cn.outerMethod = om;
        }
        String od = outerdesc.getText();
        if (od.isEmpty()) {
          cn.outerMethodDesc = null;
        } else {
          cn.outerMethodDesc = od;
        }
        if (refresh) {
          JByteMod.instance.refreshTree();
        }
      }
    });
    this.add(update, BorderLayout.PAGE_END);
    this.show();
  }

  @Override
  public void setVisible(boolean aFlag) {
    if (!aFlag && !(getLocation().getY() == 0 && getLocation().getX() == 0)) {
      bounds = getBounds();
    }
    super.setVisible(aFlag);
  }
}
