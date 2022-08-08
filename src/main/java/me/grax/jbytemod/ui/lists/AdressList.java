package me.grax.jbytemod.ui.lists;

import java.awt.Font;

import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JList;

import me.grax.jbytemod.ui.lists.entries.InstrEntry;
import me.grax.jbytemod.utils.gui.SwingUtils;
import me.grax.jbytemod.utils.list.LazyListModel;

public class AdressList extends JList<String> {
  private MyCodeList cl;

  public AdressList(MyCodeList cl) {
    super(new DefaultListModel<String>());
    this.cl = cl;
    cl.setAdressList(this);
    this.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
    this.updateAdr();
    this.setSelectionModel(new DefaultListSelectionModel() {
      @Override
      public void setSelectionInterval(int index0, int index1) {
        super.setSelectionInterval(-1, -1);
      }
    });
    this.setPrototypeCellValue("0000");
    SwingUtils.disableSelection(this);
  }

  public void updateAdr() {
    LazyListModel<String> lm = new LazyListModel<String>();
    LazyListModel<InstrEntry> clm = (LazyListModel<InstrEntry>) cl.getModel();
    if (clm.getSize() > 9999) {
      throw new RuntimeException("code too big");
    }
    for (int i = 0; i < clm.getSize(); i++) {
      String hex = String.valueOf(i);
      lm.addElement("0000".substring(hex.length()) + hex);
    }
    this.setModel(lm);
  }

}
