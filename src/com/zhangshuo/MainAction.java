package com.zhangshuo;


import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.zhangshuo.UI.MainDialog;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class MainAction extends AnAction {

    private MainDialog dialog;

    @Override
    public void actionPerformed(AnActionEvent event) {
        //获取项目跟路径
        dialog=new MainDialog();
        dialog.setEvent(event);
        dialog.setSize(400, 450);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        dialog.requestFocus();
    }


}
