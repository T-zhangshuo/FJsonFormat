# FJsonFromat

Flutter Json数据转成Model的IDEA插件包。

#### 使用方法：
1、下载FJsonFormat.jar文件，以离线方式安装到IDEA中。

2、创建一个新到dart文件，在文件中 打开Generate菜单，选择FJsonFormat。

3、输入json格式数据，点击确认即可创建。

4、勾选 自动YMAL，则会自动在配置文件中，加入 json_annotation 和 build_run的依赖。

5、JSON数据中，如果需要引入其他类的功能，可以以 ``$ClassName`` 引入，或者 ``$[]ClassName``.


#### 示例JSON数据

Child.dart
```
{
  "name":"name",
  "age":18
}
```

Person.dart
```
{
  "name": "John Smith",
  "email": "john@example.com",
  "mother":"$Person",
  "son": "$Child",
  "girls":"$[]Child",
  "friends":"$[]Person"
}
```