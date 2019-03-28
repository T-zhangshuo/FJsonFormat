package com.zhangshuo.UI;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.zhangshuo.Utils.ConfigUtils;
import com.zhangshuo.Utils.FileUtils;
import org.apache.velocity.texen.util.FileUtil;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.swing.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class MainDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JCheckBox checkBoxUpdate;
    private JTextArea textArea1;
    private JLabel labelTip;
    private AnActionEvent event;


    private static final String json_annotation_Version = "^2.0.0";
    private static final String build_runner_Version = "^1.0.0";

    private static final String TEMPLATE = "import 'package:json_annotation/json_annotation.dart';\n" +
            "%t\n" +
            "part '%s.g.dart';\n" +
            "@JsonSerializable()\n" +
            "class %s {\n" +
            "  %s();\n" +
            "\n" +
            "  %p\n" +
            "  factory %s.fromJson(Map<String,dynamic> json) => _$%sFromJson(json);\n" +
            "  Map<String, dynamic> toJson() => _$%sToJson(this);\n" +
            "  static List<%s> fromListJson(jsonList){\n"+
            "    return jsonList.map((map) {\n"+
            "      return %s.fromJson(map);\n"+
            "    }).toList();\n"+
            "  }\n"+
            "}";

    private static final String TEMPLATE_G = "part of '%s.dart';\n" +
            "%s _$%sFromJson(Map<String,dynamic> json){\n" +
            "\treturn %s()" +
            "%p;\n" +
            "}\n" +
            "Map<String,dynamic> _$%sToJson(%s instance) => \n" +
            "\t<String,dynamic>{\n" +
            "%d};";

    public MainDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

    }

    private void onOK() {
        // add your code here
        // 检查是否已经勾选
        boolean isSelect = checkBoxUpdate.isSelected();
        if (isSelect) {
            Project project = event.getData(PlatformDataKeys.PROJECT);
            String pubspecPath = project.getBasePath() + "/pubspec.yaml";
            System.out.println(pubspecPath);
            ConfigUtils.checkYaml(pubspecPath, json_annotation_Version, build_runner_Version);
        }
        // 分析
        String jsonStr = textArea1.getText();
        Map map = new Gson().fromJson(jsonStr, Map.class);
        if (map == null || map.size() == 0) {
            labelTip.setText("错误的JsonObject 格式");
        } else {
            labelTip.setText("正确的JsonObject 格式，正在生成Model");
            displayObject(map);
        }
    }

    private void displayObject(Map map) {
        //读取模版文件内容
        //获取项目的当前文件地址
        VirtualFile virtualFile = event.getData(PlatformDataKeys.VIRTUAL_FILE);
        String modelFilePath = virtualFile.getPath();
        //文件名
        String modelFileName = modelFilePath.substring(modelFilePath.lastIndexOf("/") + 1, modelFilePath.lastIndexOf("."));

        //导入的头
        List<String> importList = new ArrayList<>();

        //对应的属性值
        StringBuffer attrs = new StringBuffer();
        StringBuffer attrs_g = new StringBuffer();
        StringBuffer attrs_gd = new StringBuffer();
        for (Object key : map.keySet()) {
            String keyStr = String.valueOf(key);
            if (keyStr.startsWith("_")) return;
            String type = getType(map.get(key), modelFileName, importList);
            attrs.append(type);
            attrs.append(" ");
            attrs.append(keyStr);
            attrs.append(";\n");
            attrs.append("  ");

            switch (type) {
                case "bool":
                case "num":
                case "Map<String,dynamic>":
                case "List":
                case "String":
                    break;
                default:
            }
            if ("bool".equals(type) ||
                    "num".equals(type) || "Map<String,dynamic>".equals(type)
                    || "List".equals(type) || "String".equals(type)) {
                attrs_g.append("\n\t\t.." + keyStr + " = json['" + keyStr + "'] as " + type);
            } else {
                if (type.startsWith("List<")) {
                    String valueClassName = map.get(key).toString().substring(3);
                    attrs_g.append("\n\t\t.." + keyStr + " = (json['" + keyStr + "'] as List )" +
                            "?.map((e) => e == null ? null: " + valueClassName + ".fromJson(e as Map<String,dynamic>))?.toList()");
                } else {
                    String valueClassName = map.get(key).toString().substring(1);
                    if (valueClassName.equals(type)) {
                        attrs_g.append("\n\t\t.." + keyStr + " = json['" + keyStr + "'] ==null ? null : " +
                                valueClassName + ".fromJson(json['" + keyStr + "'] as Map<String,dynamic> ");
                    }
                }
            }
            attrs_gd.append("'" + keyStr + "': instance." + keyStr + ",");
        }

        //读取模版内容，并且全部替换
        String fileContent = TEMPLATE;
        StringBuffer importAttrs = new StringBuffer();
        for (int i = 0; i < importList.size(); i++) {
            String importStr = "import '" + importList.get(i) + ".dart';";
            importAttrs.append(importStr);
            importAttrs.append("\n");
        }
        fileContent = fileContent.replaceAll("%t", importAttrs.toString());
        fileContent = fileContent.replaceAll("%s", modelFileName);
        fileContent = fileContent.replaceAll("%p", attrs.toString());

        FileUtils.writeFile(modelFilePath, fileContent);

        //生成模版模型

        String fileGContent = TEMPLATE_G;
        fileGContent = fileGContent.replaceAll("%s", modelFileName);
        //%p
        fileGContent = fileGContent.replaceAll("%p", attrs_g.toString());
        //%d
        fileContent = fileGContent.replaceAll("%d", attrs_gd.toString().substring(0, attrs_gd.toString().length() - 1));

        String gModelFile = modelFilePath.replace(".dart", ".g.dart");
        FileUtils.writeFile(gModelFile, fileContent);

        dispose();
    }

    private String getType(Object value, String name, List<String> importList) {
        String lowername = name.toLowerCase();
        if (value instanceof Boolean) {
            return "bool";
        } else if (value instanceof Double || value instanceof Long || value instanceof Integer || value instanceof Float) {
            return "num";
        } else if (value instanceof Map) {
            return "Map<String,dynamic>";
        } else if (value instanceof List) {
            return "List";
        } else if (value instanceof String) {
            //处理特殊标记
            String valueStr = (String) value;
            if (valueStr.startsWith("$[]")) {
                //如果为这个类开头的，则后面的就为这个文件
                String valueClassName = valueStr.substring(3);
                if (!valueClassName.toLowerCase().equals(lowername)) {
                    //如果不是同一个文件类
                    if (importList.indexOf(valueClassName) == -1) {
                        importList.add(valueClassName);
                    }
                    return "List<" + valueClassName + ">";
                } else {
                    return "List<" + name + ">";
                }
            } else if (valueStr.startsWith("$")) {
                String valueClassName = valueStr.substring(1);
                if (!valueClassName.toLowerCase().equals(lowername)) {
                    //如果不是同一个文件类
                    if (importList.indexOf(valueClassName) == -1) {
                        importList.add(valueClassName);
                    }
                    return valueClassName;
                } else {
                    return name;
                }
            }
        }
        return "String";
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public void setEvent(AnActionEvent event) {
        this.event = event;
    }

    public static void main(String[] args) {
        MainDialog dialog = new MainDialog();
        dialog.pack();
        dialog.setSize(400, 450);
        dialog.setVisible(true);
        System.exit(0);
    }
}
