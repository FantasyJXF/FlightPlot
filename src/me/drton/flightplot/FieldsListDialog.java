package me.drton.flightplot;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.util.*;

public class FieldsListDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonAdd; // Add按钮
    private JTable fieldsTable; // 字段列表内容
    private JButton buttonClose; // Close按钮
    private DefaultTableModel fieldsTableModel;

    public FieldsListDialog(final Runnable callbackAdd) {
        setContentPane(contentPane);
        setModal(true);
        setTitle("字段列表");
        getRootPane().setDefaultButton(buttonAdd);
        buttonAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                callbackAdd.run();
            }
        });
        buttonClose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onClose();
            }
        });
        // call onClose() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onClose();
            }
        });
        // call onClose() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onClose();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onClose() {
        setVisible(false);
    }

    public void setFieldsList(Map<String, String> fields) { // 设置字段列表，键
        while (fieldsTableModel.getRowCount() > 0) {
            fieldsTableModel.removeRow(0);
        }
        List<String> fieldsList = new ArrayList<String>(fields.keySet()); // 设置键
        Collections.sort(fieldsList); // 键的大小排序
        for (String field : fieldsList) {
            fieldsTableModel.addRow(new Object[]{field, fields.get(field)});
        }
    }

    public List<String> getSelectedFields() {
        List<String> selectedFields = new ArrayList<String>();
        for (int i : fieldsTable.getSelectedRows()) {
            selectedFields.add((String) fieldsTableModel.getValueAt(i, 0)); // 选中的行/字段
        }
        return selectedFields;
    }

    private void createUIComponents() {
        // Fields table
        fieldsTableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        fieldsTableModel.addColumn("字段");
        fieldsTableModel.addColumn("数据类型");
        fieldsTable = new JTable(fieldsTableModel);
    }
}
