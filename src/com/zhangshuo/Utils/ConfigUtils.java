package com.zhangshuo.Utils;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ConfigUtils {

    public static void checkYaml(String pubspecPath, String json_annotation_Version, String build_runner_Version) {
        File file = new File(pubspecPath);
        try {
            copyFile(file, new File(file.getPath() + ".bak"));
            //读取文件
            Yaml yaml = new Yaml();
            Map map = yaml.load(new FileInputStream(file));
            System.out.println(map);
            //判断是否已经存在该对应的配置
            Map dependenciess = (Map) map.get("dependencies");
            if (dependenciess == null) {
                //创建dependencies
                //创建 > json_annotation: ^2.0.0
                Map jsonMap = new HashMap();
                jsonMap.put("json_annotation", json_annotation_Version);
                map.put("dependencies", jsonMap);
            } else {
                Object json_annotation = dependenciess.get("json_annotation");
                if (json_annotation == null) {
                    //创建 > json_annotation: ^2.0.0
                    ((Map) map.get("dependencies")).put("json_annotation", json_annotation_Version);
                }
            }

            //判断dev_dependencies
            Map devDependencies = (Map) map.get("dev_dependencies");
            if (devDependencies == null) {
                //创建 dev_dependencies
                //创建   build_runner: ^1.0.0
                //      json_serializable: ^2.0.0
                Map jsonMap = new HashMap();
                jsonMap.put("build_runner", "^1.0.0");
                jsonMap.put("json_serializable", "^2.0.0");
                map.put("dev_dependencies", jsonMap);
            } else {
                Object build_runner = devDependencies.get("build_runner");
                Object json_serializable = devDependencies.get("json_serializable");

                if (build_runner == null) {
                    //创建 build_runner: ^1.0.0
                    ((Map) map.get("dev_dependencies")).put("build_runner", build_runner_Version);
                }
                if (json_serializable == null) {
                    //创建 json_serializable: ^2.0.0
                    ((Map) map.get("dev_dependencies")).put("json_serializable", json_annotation_Version);
                }
            }
            System.out.println(map);
            //写入map
            DumperOptions dumperOptions = new DumperOptions();
            dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            dumperOptions.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
            dumperOptions.setPrettyFlow(false);
            Yaml writeYaml = new Yaml(dumperOptions);
            writeYaml.dump(map, new OutputStreamWriter(new FileOutputStream(file)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void copyFile(File sourceFile, File targetFile)
            throws IOException {
        // 新建文件输入流并对它进行缓冲
        FileInputStream input = new FileInputStream(sourceFile);
        BufferedInputStream inBuff = new BufferedInputStream(input);

        // 新建文件输出流并对它进行缓冲
        FileOutputStream output = new FileOutputStream(targetFile);
        BufferedOutputStream outBuff = new BufferedOutputStream(output);

        // 缓冲数组
        byte[] b = new byte[1024 * 5];
        int len;
        while ((len = inBuff.read(b)) != -1) {
            outBuff.write(b, 0, len);
        }
        // 刷新此缓冲的输出流
        outBuff.flush();

        //关闭流
        inBuff.close();
        outBuff.close();
        output.close();
        input.close();
    }
}
