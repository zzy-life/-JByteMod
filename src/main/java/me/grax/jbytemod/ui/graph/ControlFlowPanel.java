package me.grax.jbytemod.ui.graph;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.objectweb.asm.tree.MethodNode;

import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxCellRenderer;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.analysis.block.Block;
import me.grax.jbytemod.analysis.converter.Converter;
import me.grax.jbytemod.ui.graph.CFGraph.CFGComponent;
import me.grax.jbytemod.utils.ErrorDisplay;
import me.grax.jbytemod.utils.ImageUtils;

public class ControlFlowPanel extends JPanel {

  private MethodNode node;
  private ArrayList<Block> cf = new ArrayList<>();
  private CFGraph graph;
  private CFGComponent graphComponent;
  private JScrollPane scp;

  private static final String edgeColor = "#111111";
  private static final String jumpColor = "#39698a";

  private static final String jumpColorGreen = "#388a47";
  private static final String jumpColorRed = "#8a3e38";
  private static final String jumpColorPurple = "#ff71388a";
  private static final String jumpColorPink = "#ba057a"; //8a386d

  public ControlFlowPanel(JByteMod jbm) {
    this.setLayout(new BorderLayout(0, 0));
    graph = new CFGraph(jbm);
    JPanel lpad = new JPanel();
    lpad.setBorder(new EmptyBorder(1, 5, 0, 1));
    lpad.setLayout(new GridLayout());
    lpad.add(new JLabel(JByteMod.res.getResource("ctrl_flow_vis")));
    JPanel rs = new JPanel();
    rs.setLayout(new GridLayout(1, 5));
    for (int i = 0; i < 3; i++)
      rs.add(new JPanel());
    JButton save = new JButton(JByteMod.res.getResource("save"));
    save.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        if (node == null) {
          return;
        }
        File parentDir = new File(System.getProperty("user.home") + File.separator + "Desktop");
        JFileChooser jfc = new JFileChooser(parentDir);
        jfc.setAcceptAllFileFilterUsed(false);
        jfc.setFileFilter(new FileNameExtensionFilter("Bitmap image file (.bmp)", "bmp"));
        jfc.addChoosableFileFilter(new FileNameExtensionFilter("Portable Network Graphics (.png)", "png"));
        if (node.name.length() < 32) {
          jfc.setSelectedFile(new File(parentDir, node.name + ".bmp"));
        } else {
          jfc.setSelectedFile(new File(parentDir, "method.bmp"));
        }
        int result = jfc.showSaveDialog(ControlFlowPanel.this);
        if (result == JFileChooser.APPROVE_OPTION) {
          File output = jfc.getSelectedFile();
          String type = ((FileNameExtensionFilter) jfc.getFileFilter()).getExtensions()[0];
          JByteMod.LOGGER.log("Saving graph as " + type + " file (" + output.getName() + ")");
          BufferedImage image = mxCellRenderer.createBufferedImage(graph, null, 1, Color.WHITE, true, null);
          try {
            ImageIO.write(ImageUtils.watermark(image), type, output);
          } catch (IOException e1) {
            new ErrorDisplay(e1);
          }
        }
      }
    });
    rs.add(save);
    JButton reload = new JButton(JByteMod.res.getResource("reload"));
    reload.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        generateList();
      }
    });
    rs.add(reload);
    lpad.add(rs);
    this.add(lpad, BorderLayout.NORTH);

    graphComponent = graph.getComponent();
    graphComponent.setScp(scp);
    JPanel inner = new JPanel();
    inner.setBorder(new EmptyBorder(30, 30, 30, 30));
    inner.setLayout(new BorderLayout(0, 0));
    inner.setBackground(Color.WHITE);
    inner.add(graphComponent, BorderLayout.CENTER);
    scp = new JScrollPane(inner);
    scp.getVerticalScrollBar().setUnitIncrement(16);
    this.add(scp, BorderLayout.CENTER);
  }

  public MethodNode getNode() {
    return node;
  }

  public void setNode(MethodNode node) {
    this.node = node;
  }

  public void generateList() {
    if (node == null)
      return;
    cf.clear();
    if (node.instructions.size() == 0) {
      this.clear();
      return;
    }
    graphComponent.setScp(scp);
    Converter c = new Converter(node);
    try {
      cf.addAll(c.convert(JByteMod.ops.get("simplify_graph").getBoolean(), JByteMod.ops.get("remove_redundant").getBoolean(), true,
          JByteMod.ops.get("max_redundant_input").getInteger()));
    } catch (Exception e) {
      e.printStackTrace();
      new ErrorDisplay(e);
      this.clear();
      return;
    }
    Object parent = graph.getDefaultParent();
    graph.getModel().beginUpdate();
    try {
      graph.removeCells(graph.getChildCells(graph.getDefaultParent(), true, true));
      existing.clear();
      if (!cf.isEmpty()) {
        boolean first = true;
        for (Block b : cf) {
          if (b.getInput().isEmpty() || first) {
            addBlock((mxCell) parent, b, null);
            first = false;
          }
        }
      }
      graph.getView().setScale(1);
      PatchedHierarchicalLayout layout = new PatchedHierarchicalLayout(graph);
      layout.setFineTuning(true);
      layout.setIntraCellSpacing(25d);
      layout.setInterRankCellSpacing(80d);
      layout.setDisableEdgeStyle(true);
      layout.setParallelEdgeSpacing(100d);
      layout.setUseBoundingBox(true);
      layout.execute(graph.getDefaultParent());
      /*
       * Alternative layout, prettier but can't handle complicated flow
       * mxCompactTreeLayout layout = new mxCompactTreeLayout(graph);
       * layout.setResetEdges(true); layout.setEdgeRouting(true);
       * layout.setHorizontal(false); layout.setMoveTree(true);
       * layout.setUseBoundingBox(true);
       * layout.execute(graph.getDefaultParent());
       **/
    } finally {
      graph.getModel().endUpdate();
    }
    this.revalidate();
    this.repaint();
    //TODO set horizontal scroll to half
  }

  private HashMap<Block, mxCell> existing = new HashMap<>();

  private mxCell addBlock(mxCell parent, Block b, BlockVertex input) {
    mxCell v1 = null;
    if (existing.containsKey(b)) {
      mxCell cached = existing.get(b);
      if (input != null) {
        ((BlockVertex) cached.getValue()).addInput(input);
      }
      return cached;
    }
    BlockVertex vertex = new BlockVertex(node, b, b.getNodes(), b.getLabel(), node.instructions.indexOf(b.getNodes().get(0)));
    if (input != null) {
      vertex.addInput(input);
    }
    vertex.setupText();
    v1 = (mxCell) graph.insertVertex(parent, null, vertex, 150, 10, 80, 40, "fillColor=#FFFFFF;fontColor=#111111;strokeColor=#9297a1");
    graph.updateCellSize(v1); //resize cell

    existing.put(b, v1);
    if (v1 == null) {
      throw new RuntimeException();
    }
    ArrayList<Block> next = b.getOutput();
    for (int i = 0; i < next.size(); i++) {
      Block out = next.get(i);
      if (out.equals(b)) {
        graph.insertEdge(parent, null, null, v1, v1, "strokeColor=" + getEdgeColor(b, i) + ";");
      } else {
        assert (out.getInput().contains(b));
        mxCell vertexOut = addBlock(parent, out, vertex);
        graph.insertEdge(parent, null, null, v1, vertexOut, "strokeColor=" + getEdgeColor(b, i) + ";");
      }
    }
    return v1;
  }

  private String getEdgeColor(Block b, int i) {
    if (b.endsWithJump()) {
      if (b.getOutput().size() > 1) {
        if (i == 0) {
          return jumpColorGreen;
        }
        return jumpColorRed;
      }
      return jumpColor;
    }
    if (b.endsWithSwitch()) {
      if (i == 0) {
        return jumpColorPink;
      }
      return jumpColorPurple;
    }
    return edgeColor;
  }

  public void clear() {
    graph.getModel().beginUpdate();
    graph.removeCells(graph.getChildCells(graph.getDefaultParent(), true, true));
    graph.getModel().endUpdate();
  }
}
